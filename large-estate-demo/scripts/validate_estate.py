#!/usr/bin/env python3
"""Static consistency checks for the Acme Industrial large estate demo.

This deliberately avoids requiring Orbital, Taxi or Nebula binaries. It checks the
contracts we can prove from the repository itself: OpenAPI validity/coverage,
Nebula route coverage, Taxi scalar references and primitive compatibility,
database Taxi-to-DDL compatibility, response fixtures, and example question sources.
"""
from __future__ import annotations

from pathlib import Path
import json
import re
import sys
from collections import Counter

import yaml
try:
    from jsonschema import Draft7Validator, RefResolver
except ImportError:  # pragma: no cover
    Draft7Validator = RefResolver = None

ROOT = Path(__file__).resolve().parents[1]
HTTP_METHODS = {"get", "post", "put", "patch", "delete", "head", "options"}
PRIMITIVES = {"String", "Int", "Decimal", "Boolean", "Date", "Instant", "Time", "Any"}
OPENAPI_PRIMITIVE = {"string": "String", "integer": "Int", "number": "Decimal", "boolean": "Boolean"}

errors: list[str] = []
notes: list[str] = []

def fail(msg: str) -> None:
    errors.append(msg)

def load_specs():
    specs = {}
    for path in sorted((ROOT / "openapi").glob("*.openapi.yaml")):
        slug = path.name.split(".")[0]
        try:
            specs[slug] = yaml.safe_load(path.read_text())
        except Exception as e:
            fail(f"Cannot parse {path.relative_to(ROOT)}: {e}")
    return specs

def openapi_operations(specs):
    rows = []
    for slug, spec in specs.items():
        for path, item in (spec.get("paths") or {}).items():
            for method, op in item.items():
                if method.lower() not in HTTP_METHODS:
                    continue
                rows.append((slug, method.lower(), f"/{slug}{path}", op))
    return rows

def parse_nebula_routes():
    text = (ROOT / "orbital/nebula/acme-estate.nebula.kts").read_text()
    routes = []
    # All generated handlers have a three-line outer shape; handler bodies can contain nested expressions.
    matches = list(re.finditer(r'^\s*(get|post|put|patch|delete|head|options)\("([^"]+)"\) \{ call ->\s*$', text, re.M))
    for i, m in enumerate(matches):
        start = m.end()
        end = matches[i + 1].start() if i + 1 < len(matches) else text.find("\n   }\n\n   postgres", start)
        if end == -1:
            end = len(text)
        routes.append((m.group(1), m.group(2), text[start:end]))
    return routes

def parse_taxonomy():
    text = (ROOT / "src/taxonomy/acme-types.taxi").read_text()
    bases = {}
    for nm in re.finditer(r'namespace\s+([\w.]+)\s*\{(.*?)\n\}', text, re.S):
        ns, body = nm.group(1), nm.group(2)
        for tm in re.finditer(r'\btype\s+(\w+)\s+inherits\s+([\w.]+)', body):
            name, base = tm.group(1), tm.group(2)
            fq = f"{ns}.{name}"
            if "." not in base and base not in PRIMITIVES:
                base = f"{ns}.{base}"
            bases[fq] = base
    return bases

def primitive_of(t: str, bases, seen=None):
    if t in PRIMITIVES:
        return t
    seen = set() if seen is None else seen
    if t in seen:
        return None
    seen.add(t)
    if t not in bases:
        return None
    return primitive_of(bases[t], bases, seen)

def iter_nodes(obj, where=""):
    if isinstance(obj, dict):
        yield where, obj
        for k, v in obj.items():
            yield from iter_nodes(v, f"{where}.{k}" if where else str(k))
    elif isinstance(obj, list):
        for i, v in enumerate(obj):
            yield from iter_nodes(v, f"{where}[{i}]")

def expected_openapi_primitive(node):
    typ = node.get("type")
    if typ == "string" and node.get("format") == "date":
        return "Date"
    if typ == "string" and node.get("format") == "date-time":
        return "Instant"
    return OPENAPI_PRIMITIVE.get(typ)

def parse_ddl():
    sql = (ROOT / "database/schema-and-seed.sql").read_text()
    tables = {}
    for m in re.finditer(r'CREATE TABLE\s+(\w+)\s*\((.*?)\n\);', sql, re.S | re.I):
        table, body = m.group(1), m.group(2)
        cols = {}
        for raw in body.splitlines():
            line = raw.strip().rstrip(',')
            if not line or line.upper().startswith(("PRIMARY KEY", "FOREIGN KEY", "UNIQUE", "CONSTRAINT")):
                continue
            cm = re.match(r'(\w+)\s+(.+?)(?:\s+NOT NULL|\s+NULL)?$', line, re.I)
            if cm:
                cols[cm.group(1)] = cm.group(2).strip().lower()
        tables[table] = cols
    return tables

def sql_primitive(sqltype):
    if sqltype.startswith(("varchar", "text", "char")):
        return "String"
    if sqltype.startswith(("integer", "int", "bigint", "smallint")):
        return "Int"
    if sqltype.startswith(("numeric", "decimal", "real", "double")):
        return "Decimal"
    if sqltype.startswith("boolean"):
        return "Boolean"
    if sqltype.startswith("date"):
        return "Date"
    if sqltype.startswith("timestamp"):
        return "Instant"
    return None

def parse_db_taxi():
    result = []
    writes = []
    for path in sorted((ROOT / "src/databases").glob("*.taxi")):
        text = path.read_text()
        ns_match = re.search(r'namespace\s+([\w.]+)', text)
        ns = ns_match.group(1) if ns_match else ""
        domain = ns.rsplit(".", 1)[-1]
        # Association between @Table annotation and its immediately following model.
        for m in re.finditer(r'@Table\((.*?)\)\s*closed model\s+(\w+)\s*\{(.*?)\n\s*\}', text, re.S):
            ann, model, body = m.groups()
            tm = re.search(r'table\s*=\s*"([^"]+)"', ann)
            if not tm:
                continue
            table = tm.group(1)
            fields = {}
            for raw in body.splitlines():
                line = raw.strip()
                if not line or line.startswith("[["):
                    continue
                fm = re.match(r'(?:@Id\s+)?(\w+)\s*:\s*([\w.]+)(\?)?', line)
                if fm:
                    fields[fm.group(1)] = fm.group(2)
            result.append((domain, table, model, fields))
        for wm in re.finditer(r'@(?:UpsertOperation|InsertOperation|UpdateOperation)[^\n]*\n\s*write operation\s+(\w+)', text):
            writes.append((domain, wm.group(1)))
    return result, writes

def validate_response_literals(specs, ops, nebula_routes):
    if Draft7Validator is None:
        notes.append("jsonschema is not installed; skipped Nebula JSON response-schema validation")
        return 0
    op_map = {(method, fullpath):(specs[slug], op) for slug, method, fullpath, op in ops}
    checked = 0
    variants = 0
    dynamic = 0
    for method, path, block in nebula_routes:
        pair = op_map.get((method, path))
        if not pair:
            continue
        spec, op = pair
        response_schema = None
        for code, resp in (op.get("responses") or {}).items():
            if str(code).startswith("2"):
                response_schema = (((resp or {}).get("content") or {}).get("application/json") or {}).get("schema")
                if response_schema:
                    break
        if not response_schema:
            continue
        literals = re.findall(r'"""(.*?)"""', block, re.S)
        if not literals and ".json()" in block:
            # Built by a Kotlin fixture class's json() function; not checkable statically.
            dynamic += 1
            continue
        if not literals:
            fail(f"Nebula {method.upper()} {path}: no JSON fixture literal found")
            continue
        for raw in literals:
            raw = re.sub(r'\$\{.*?\}', 'DUMMY', raw)
            try:
                data = json.loads(raw)
            except Exception as e:
                fail(f"Nebula {method.upper()} {path}: invalid JSON fixture: {e}: {raw[:120]}")
                continue
            if isinstance(data, dict) and set(data) == {"error"}:
                # Error (non-2xx) response body.
                continue
            try:
                resolver = RefResolver.from_schema(spec)
                Draft7Validator(response_schema, resolver=resolver).validate(data)
            except Exception as e:
                fail(f"Nebula {method.upper()} {path}: fixture violates OpenAPI response: {getattr(e, 'message', e)}")
            variants += 1
        checked += 1
    notes.append(f"Response schemas: {checked} handlers / {variants} fixture variants checked ({dynamic} handlers use Kotlin fixture classes and are not checked)")
    return variants

def main():
    specs = load_specs()
    ops = openapi_operations(specs)
    neb = parse_nebula_routes()
    bases = parse_taxonomy()
    ddl = parse_ddl()
    db_models, db_writes = parse_db_taxi()

    # OpenAPI / operation inventory.
    operation_ids = [op.get("operationId") for _,_,_,op in ops]
    for oid, n in Counter(operation_ids).items():
        if not oid:
            fail("OpenAPI operation without operationId")
        elif n > 1:
            fail(f"Duplicate OpenAPI operationId: {oid} ({n} occurrences)")
    if len(ops) < 100:
        fail(f"Expected at least 100 HTTP operations, found {len(ops)}")

    # HTTP route parity between OpenAPI and Nebula.
    expected = {(method,path) for _,method,path,_ in ops}
    actual = {(method,path) for method,path,_ in neb}
    for x in sorted(expected - actual): fail(f"Missing Nebula route for OpenAPI: {x[0].upper()} {x[1]}")
    for x in sorted(actual - expected): fail(f"Nebula route has no OpenAPI operation: {x[0].upper()} {x[1]}")

    # Every scalar OpenAPI annotation must resolve to a Taxi primitive compatible with its physical shape.
    scalar_mappings = 0
    for slug, spec in specs.items():
        for where, node in iter_nodes(spec):
            tx = node.get("x-taxi-type") if isinstance(node, dict) else None
            if not isinstance(tx, dict) or not tx.get("name"):
                continue
            expected_primitive = expected_openapi_primitive(node)
            if not expected_primitive:  # object-level x-taxi-type creates/names a model, not a scalar.
                continue
            name = tx["name"]
            got = primitive_of(name, bases)
            scalar_mappings += 1
            if got is None:
                fail(f"{slug}:{where}: scalar x-taxi-type {name} is not declared in shared taxonomy")
            elif got != expected_primitive:
                fail(f"{slug}:{where}: physical {expected_primitive}, Taxi {name} resolves to {got}")

    # DB physical columns vs Taxi field semantics.
    db_mappings = 0
    db_table_sources = set()
    for domain, table, model, fields in db_models:
        db_table_sources.add(f"db.{domain}.{table}")
        if table not in ddl:
            fail(f"Taxi @Table {table} has no CREATE TABLE in database/schema-and-seed.sql")
            continue
        sqlcols = ddl[table]
        for col, sem in fields.items():
            if col not in sqlcols:
                fail(f"Taxi model {model}.{col} has no physical column {table}.{col}")
                continue
            expected_primitive = sql_primitive(sqlcols[col])
            got = primitive_of(sem, bases)
            db_mappings += 1
            if got is None:
                fail(f"Taxi model {model}.{col}: semantic type {sem} not declared")
            elif expected_primitive and got != expected_primitive:
                fail(f"DB {table}.{col}: SQL {sqlcols[col]} -> {sem} -> {got}, expected {expected_primitive}")
        missing = set(sqlcols) - set(fields)
        if missing:
            fail(f"Taxi model {model} omits physical columns from {table}: {sorted(missing)}")
    taxi_tables = {table for _,table,_,_ in db_models}
    for table in sorted(set(ddl) - taxi_tables):
        fail(f"DDL table {table} has no Taxi @Table model")

    validate_response_literals(specs, ops, neb)

    # Example question sources must point at a real capability.
    http_sources = {f"{slug}.{op['operationId']}" for slug,_,_,op in ops}
    db_write_sources = {f"db.{domain}.{op}" for domain, op in db_writes}
    valid_sources = http_sources | db_table_sources | db_write_sources
    questions = yaml.safe_load((ROOT / "examples/questions.yaml").read_text()).get("questions", [])
    for c in questions:
        for src in c.get("sources", []):
            if src not in valid_sources:
                fail(f"Question {c.get('id')}: unknown source/capability {src}")

    # Manifest/catalog count consistency.
    manifest = yaml.safe_load((ROOT / "catalog/estate-manifest.yaml").read_text())
    expected_counts = manifest.get("estate", {})
    counts = {
        "http_apis": len(specs),
        "http_operations": len(ops),
        "database_domains": len({d for d,_,_,_ in db_models}),
        "database_tables": len(db_models),
        "database_writes": len(db_writes),
    }
    counts["total_capabilities"] = counts["http_operations"] + counts["database_tables"] + counts["database_writes"]
    for k,v in counts.items():
        if expected_counts.get(k) != v:
            fail(f"Manifest {k}={expected_counts.get(k)!r}, actual={v}")

    # Flat operation catalog should have exactly one row per capability.
    catalog = json.loads((ROOT / "catalog/operations.json").read_text())
    if len(catalog) != counts["total_capabilities"]:
        fail(f"catalog/operations.json has {len(catalog)} rows, expected {counts['total_capabilities']}")

    print("Acme Industrial estate static validation")
    print(f"HTTP APIs: {counts['http_apis']}")
    print(f"HTTP operations: {counts['http_operations']}")
    print(f"Nebula HTTP handlers: {len(neb)}")
    print(f"DB domains: {counts['database_domains']}")
    print(f"DB tables: {counts['database_tables']}")
    print(f"DB writes: {counts['database_writes']}")
    print(f"Total capabilities: {counts['total_capabilities']}")
    print(f"OpenAPI scalar semantic mappings: {scalar_mappings}")
    print(f"DB scalar semantic mappings: {db_mappings}")
    print(f"Example questions: {len(questions)}")
    for note in notes:
        print(note)
    print(f"Errors: {len(errors)}")
    for e in errors:
        print(f"ERROR: {e}")
    return 1 if errors else 0

if __name__ == "__main__":
    sys.exit(main())
