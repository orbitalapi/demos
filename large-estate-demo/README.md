# Large estate demo

A large, deliberately messy, near-real-world estate for an industrial distributor, Acme Industrial.
It shows Orbital linking data across many independently designed systems that don't share
identifiers or representations.

```schemaDiagram
{
  "members" : {
    "com.acme.db.finance.FinanceDatabase": {},
    "com.acme.billing.InvoicesService": {},
    "com.acme.payments.PaymentsService": {},
    "com.acme.billing.Credit_notesService": {}
  }
}
```

## Estate size

- **14 HTTP APIs**
- **104 HTTP operations**
- **22 queryable database tables** across 4 independently modelled database domains
- **2 database write operations**
- **128 operations in total**

Each system keeps its own identifiers and representations. Taxi describes what the data means,
without forcing the systems onto a canonical model.

## The customer

Most of the data centres on **Northbridge Rail Engineering Ltd**, which every system knows by a different ID:

| System | Identifier |
|---|---|
| Company/legal entity | `08392811` |
| CRM | `ACC-23918` |
| ERP/billing | `D001922` |
| Portal identity | `ORG-881` |
| Portal user (Sarah Jones) | `U-104` |
| Legacy Northstar customer | `NSC-441` |

The main delayed order is `ORD-38291`. Its problem product `ACM-7710-X` maps to warehouse SKU `7710X`,
supplier `SUP-42`, supplier part `QZ-7710/UK` and legacy SKU `NX7710`.

## Scenario: order to cash

This is the cash reconciliation step of an *order to cash* process.
The customer has invoices in the ERP, payments in a payment provider and cash postings in the finance ledger,
and the three don't agree.

> For Northbridge Rail Engineering, list all their invoices and show whether each one has actually been paid.
> For each invoice I want the ERP status and amount, any payments the payment provider took (with status),
> any cash posted in the finance ledger, plus any disputes and credit notes against it.

The Nebula stack (`orbital/nebula/acme-estate.nebula.kts`) lists the deliberate discrepancies,
such as stale ERP statuses, double captures, transposed amounts and cash posted against the wrong invoice.

### Custom functions

The project defines some custom functions:

```taxi
   // IFRS 9 provision matrix: the loss rate rises with how long the receivable is overdue.
   // Disputed receivables are assessed at no less than 40%.
   // The rates are illustrative, not a recommendation.
   function provisionRate(daysPastDue: DaysPastDue, disputed: HasOpenDispute): ProvisionRate -> when {
      disputed == true && daysPastDue <= 180 -> 0.40
      daysPastDue <= 0 -> 0.005
      daysPastDue <= 30 -> 0.015
      daysPastDue <= 60 -> 0.04
      daysPastDue <= 90 -> 0.10
      daysPastDue <= 180 -> 0.25
      else -> 0.50
   }

   // Expected credit loss = what's still owed x the provision rate for its age and dispute status.
   // Nothing is provisioned when nothing is owed - including overpaid invoices, where exposure is negative.
   function expectedCreditLoss(exposure: NetExposure, daysPastDue: DaysPastDue, disputed: HasOpenDispute): ExpectedCreditLoss -> when {
      exposure <= 0 -> 0.0
      else -> exposure * provisionRate(daysPastDue, disputed)
   }
```

`daysBetween` is a Kotlin function, defined in `functions/DateFunctions.taxi.kts`.

The functions are then used in a query:

```taxiql
given {
   debtorCode : com.acme.erp.DebtorCode = 'D001922',
   reportingDate : com.acme.finance.ReportingDate = parseDate('2026-08-31')
}
find { com.acme.billing.Invoice[] } as (inv: com.acme.billing.Invoice) -> {
   invoiceNumber : InvoiceNumber
   erpStatus : com.acme.billing.InvoiceStatus
   invoiced : MoneyAmount
   dueDate : com.acme.billing.InvoiceDueDate
   daysPastDue : com.acme.finance.DaysPastDue = daysBetween(com.acme.billing.InvoiceDueDate, com.acme.finance.ReportingDate)
   ledgerCash : com.acme.db.finance.FinanceCashApplicationRow[](InvoiceNumber == inv::InvoiceNumber) as {
      amount : MoneyAmount
   }[]
   creditNotes : com.acme.db.finance.FinanceCreditNoteLedgerRow[](InvoiceNumber == inv::InvoiceNumber) as {
      amount : MoneyAmount
   }[]
   disputes : com.acme.db.finance.FinanceDisputeRow[](InvoiceNumber == inv::InvoiceNumber) as {
      status : com.acme.analytics.DisputeStatus
   }[]
   cashApplied : Decimal = this.ledgerCash.sum((MoneyAmount) -> MoneyAmount) ?: 0
   credited : Decimal = this.creditNotes.sum((MoneyAmount) -> MoneyAmount) ?: 0
   disputed : com.acme.finance.HasOpenDispute = this.disputes.any((com.acme.analytics.DisputeStatus) -> com.acme.analytics.DisputeStatus == 'OPEN') ?: false
   exposure : com.acme.finance.NetExposure = this.invoiced - this.cashApplied - this.credited
   provision : com.acme.finance.ExpectedCreditLoss = expectedCreditLoss(this.exposure, this.daysPastDue, this.disputed)
}[]
```

## More questions to try

`examples/questions.yaml` has 15 more questions about the same estate, each with its answer and a TaxiQL query.
They range from simple lookups to multi-system diagnosis and write workflows, for example:

- What is the current delivery status of order `ORD-38291`?
- Sarah Jones is on the phone. Show her open orders, unpaid invoices and outstanding support tickets.
- Why has order `ORD-38291` not shipped, and when can it move?
- Did Northbridge have any orders before we migrated them off Northstar, and do we still have stock under the old product codes?
- Cancel line `L-20` on `ORD-38291` and refund the customer for that line.

## Running the demo

Start Orbital with Nebula enabled, and point a workspace at this directory (`workspace.conf` is included).
Nebula starts one HTTP stub serving all 14 APIs, plus one Postgres container holding all 22 tables.

- `orbital/config/services.conf` points the `acmeApi` service at the Nebula HTTP stub.
- `orbital/config/connections.conf` points the `acme-db` JDBC connection at the Nebula Postgres container.

HTTP write stubs return realistic responses but don't keep state, so later HTTP reads don't reflect earlier writes.
Database writes are real.

## Layout

```text
src/taxonomy/acme-types.taxi           Shared semantic types
src/databases/*.taxi                   Database table models and services
src/finance/expected-credit-loss.taxi  Credit-loss types and functions
functions/*.taxi.kts                   Kotlin functions
openapi/*.openapi.yaml                 14 Taxi-annotated OpenAPI specs
openapi/*.openapi.taxi.conf            OpenAPI namespace config
orbital/nebula/acme-estate.nebula.kts  HTTP and Postgres stubs
orbital/config/                        Service and connection config
database/schema-and-seed.sql           Readable copy of the database DDL and seed data
examples/questions.yaml                Example questions, with answers and TaxiQL
catalog/estate-manifest.yaml           Summary of every system in the estate
catalog/operations.{csv,json}          Flat list of every operation
catalog/semantic-bridges.yaml          The shared facts that link systems together
docs/estate-design.md                  How the estate is modelled
scripts/validate_estate.py             Static consistency checks
```

## Design rules

1. A shared Taxi type means the values are genuinely interchangeable, not merely that "both are customer IDs".
2. System-native IDs stay distinct (`AccountId`, `DebtorCode`, `OrganisationId`, `LegacyCustomerId`).
3. Systems are linked through real facts, such as `CompanyRegistrationNumber` or mapping tables.
4. API models stay source-specific; there's no shared canonical `Customer` or `Order` DTO.
5. Inconsistent physical naming and representation are kept as-is: `bill_to`, `debtor_code`, `cust_no`,
   `item_code`, payments in minor units, invoices in major units, and so on.
6. Read-via-POST operations are marked `x-taxi-operation-kind: read`; mutations keep write semantics.
7. Lookup stubs include a decoy customer, so using the wrong identifier gives a plausible but wrong answer,
   rather than silently returning Northbridge's data.
8. The legacy migration-status endpoint reports migration state without exposing a universal ID map.

## Validation

Run `python scripts/validate_estate.py` (needs `pyyaml`, and optionally `jsonschema`).
It checks that OpenAPI routes and Nebula handlers match, semantic types are compatible,
the Taxi database models match the DDL, stub responses match their OpenAPI schemas,
and the example questions reference real operations.
