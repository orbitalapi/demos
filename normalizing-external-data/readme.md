## Overview

This demo walks through a standard ETL task: ingesting stock reports from two warehouses in different formats, normalizing them into a common structure, and persisting the results to a database — all without writing any transformation code.

The two feeds have real-world messiness: different column names, different date formats, and different product identifier schemes. We'll use [TaxiQL](https://orbitalhq.com/docs/querying/taxiql) and [semantic types](https://orbitalhq.com/docs/taxi/semantic-types) to resolve all of this automatically, then enrich the data with product names from a REST API and apply business logic to derive calculated fields.

## The raw data

The two feeds cover the same domain but are structurally different. Run each query to see the raw data:

**Sydney warehouse report:**
```taxiql
find { SydneyWarehouseStockReport[] }
```
| itemId | count | price  | location | reorderPoint | minOrder | timestamp              | lastDelivery |
|--------|-------|--------|----------|--------------|----------|------------------------|--------------|
| 1001   | 132   | 305.99 | Z-101    | 95           | 45       | 12-15-2024 02:32:15 pm | 12-10-2024   |
| 1002   | 82    | 915.99 | Y-205    | 75           | 20       | 12-15-2024 02:32:15 pm | 12-08-2024   |

**London warehouse report:**
```taxiql
find { LondonWarehouseStockReport[] }
```
| SKU        | Stock_Level | Unit_Price | Bin_Location | Reorder_Level | Min_Order_Qty | Last_Updated        | Last_Received |
|------------|-------------|------------|--------------|---------------|---------------|---------------------|---------------|
| THRUST-001 | 145         | 299.99     | A-101        | 100           | 50            | 15/12/2024 14:32:15 | 10/12/2024    |
| SHIELD-002 | 78          | 899.99     | B-205        | 80            | 25            | 15/12/2024 14:32:15 | 08/12/2024    |

The differences to resolve:

* **Column names** differ between each dataset
* **Date formats** differ between Sydney and London — neither uses a standard format
* **Product identifiers** differ — Sydney uses internal item IDs, London uses standard SKUs
* **Product names** are absent from both — we'll enrich from a REST API

## Normalizing with a TaxiQL query

The output shape of a TaxiQL query is defined using semantic types — not raw column names. Orbital uses those types to figure out how to map and convert each field from the source data, handling column name differences and date format conversions automatically.

Here's the normalizing query for Sydney. The `as { ... }` block defines the target shape using semantic types, and Orbital resolves how to populate each field from `SydneyWarehouseStockReport`:

**Sydney:**
```taxiql
find { SydneyWarehouseStockReport[] } as {
    sku: SKU
    stockLevel: StockLevel
    reorderLevel: ReorderLevel
    price: Price
    lastUpdated: WarehouseReportLastUpdated
    productName: ProductName
    stockValue: StockLevel * Price
    reorderStatus: String = when {
        RemainingBeforeReorder < 10 && RemainingBeforeReorder > 0 -> "Reorder soon"
        RemainingBeforeReorder < 0 -> "Time to reorder"
        else -> "Sufficient Stock"
    }
}[]
```

Because the output shape is defined in terms of types rather than field names, the same query works for London — just swap the `find { }` clause:

**London:**
```taxiql
find { LondonWarehouseStockReport[] } as {
    sku: SKU
    stockLevel: StockLevel
    reorderLevel: ReorderLevel
    price: Price
    lastUpdated: WarehouseReportLastUpdated
    productName: ProductName
    stockValue: StockLevel * Price
    reorderStatus: String = when {
        RemainingBeforeReorder < 10 && RemainingBeforeReorder > 0 -> "Reorder soon"
        RemainingBeforeReorder < 0 -> "Time to reorder"
        else -> "Sufficient Stock"
    }
}[]
```

A few things worth noting in these queries:

**`productName: ProductName`** — neither feed includes product names, only identifiers. Orbital sees that `ProductName` is available from a REST API, and automatically calls it using the identifier already in scope. No join configuration needed.

**`lastUpdated: WarehouseReportLastUpdated`** — Sydney and London use different date formats. Because both map to the same semantic type, Orbital normalizes them into a consistent format automatically.

**`stockValue: StockLevel * Price`** — a calculated field, derived inline from two other typed fields.

**`reorderStatus`** — uses a `when` clause to apply business logic, branching on `RemainingBeforeReorder`, which is itself a derived type defined as `StockLevel - ReorderLevel`. Clicking on a cell in the results table shows the **lineage** of how the value was computed — which source fields contributed, and what transformations were applied.

> [!NOTE]
> Check the **Profiler tab** when running each query to see the integration plan Orbital built — you'll notice the plan differs between Sydney and London, because the steps needed to resolve the same types are different for each source.

## Extracting a reusable model

The output shape is identical for both queries, so we can factor it out into a named model to avoid duplication:

```taxi
model StandardWarehouseReport {
    sku: SKU
    stockLevel: StockLevel
    reorderLevel: ReorderLevel
    price: Price
    lastUpdated: WarehouseReportLastUpdated
    productName: ProductName
    stockValue: StockLevel * Price
    reorderStatus: String = when {
        RemainingBeforeReorder < 10 && RemainingBeforeReorder > 0 -> "Reorder soon"
        RemainingBeforeReorder < 0 -> "Time to reorder"
        else -> "Sufficient Stock"
    }
}
```

The queries simplify to:

**Sydney:**
```taxiql
find { SydneyWarehouseStockReport[] } as StandardWarehouseReport[]
```

**London:**
```taxiql
find { LondonWarehouseStockReport[] } as StandardWarehouseReport[]
```

Orbital resolves the mapping from each source to `StandardWarehouseReport` automatically — the same target type, two different integration plans.

## Persisting to a database

We have a Postgres-backed model and service defined for persistence. The `@Table` annotation tells Orbital which table to write to, and `@UpsertOperation` means re-running the query will update existing records rather than creating duplicates:

```taxi
@Table(connection = "my-postgres-db", schema = "public", table = "warehouseReport")
closed parameter model DbStandardWarehouseReport {
    @Id @GeneratedId
    recordId: RecordId?
    location: WarehouseLocation
    sku: SKU
    stockLevel: StockLevel
    reorderLevel: ReorderLevel
    price: Price
    lastUpdated: WarehouseReportLastUpdated
    productName: ProductName
    stockValue: StockLevel * Price
    reorderStatus: String = when {
        RemainingBeforeReorder < 10 && RemainingBeforeReorder > 0 -> "Reorder soon"
        RemainingBeforeReorder < 0 -> "Time to reorder"
        else -> "Sufficient Stock"
    }
}

@DatabaseService(connection = "my-postgres-db")
service MyPostgresService {
    table warehouseReport : DbStandardWarehouseReport[]
    @UpsertOperation
    write operation saveWarehouseReport(DbStandardWarehouseReport): DbStandardWarehouseReport
}
```

Orbital will create the table automatically on first run if it doesn't already exist.

### Writing data

The `given { }` clause injects a constant value into the query — here, it provides `WarehouseLocation` so Orbital can populate the `location` field in the database record. The `call` clause then directs Orbital to pass the normalized results to the write operation.

**Sydney:**
```taxiql
given { WarehouseLocation = 'Sydney' }
find { SydneyWarehouseStockReport[] }
call MyPostgresService::saveWarehouseReport
```

**London:**
```taxiql
given { WarehouseLocation = 'London' }
find { LondonWarehouseStockReport[] }
call MyPostgresService::saveWarehouseReport
```

### Reading back from the database

Once you've run the above, you can query the persisted data directly:

```taxiql
find { DbStandardWarehouseReport[] }
```

## Publishing as HTTP endpoints

To make the ingestion queries triggerable over HTTP, we publish them as named endpoints. These are already defined in the schema — visit the [Endpoints page](/endpoints) to see analytics on calls made so far.

**London:**
```taxi
@HttpOperation(method = "POST", url = "/api/q/updateLondonWarehouse")
query UpdateLondonWarehouse {
    given { WarehouseLocation = 'London' }
    find { LondonWarehouseStockReport[] }
    call MyPostgresService::saveWarehouseReport
}
```

**Sydney:**
```taxi
@HttpOperation(method = "POST", url = "/api/q/updateSydneyWarehouse")
query UpdateSydneyWarehouse {
    given { WarehouseLocation = 'Sydney' }
    find { SydneyWarehouseStockReport[] }
    call MyPostgresService::saveWarehouseReport
}
```

## Exposing the normalized data via REST

Finally, the normalized database contents are published as a read endpoint:

```taxi
@HttpOperation(method = "GET", url = "/api/q/warehouseReport")
query GetWarehouseReport {
    find { DbStandardWarehouseReport[] }
}
```

```bash
curl -X GET "http://localhost:9022/api/q/warehouseReport"
```
