# Estate design notes

## What the estate shows

Acme Industrial's systems were designed independently. Answering a business question means carrying identity,
representation and workflow state across several of them. The API and database descriptions are clear and usable;
the difficulty comes from the number of systems and how loosely they're connected.

`examples/questions.yaml` records one known route through the estate for each question (`sources`).
Other routes can produce the same answer.

## Deliberate identity graph

| Business concept | System-native identity | Deliberate bridge(s) |
|---|---|---|
| Legal customer | `CompanyRegistrationNumber` | CRM account, ERP debtor, identity organisation, contracts, legacy customer |
| CRM customer | `crm.AccountId` | CRM account exposes company registration number |
| ERP customer | `erp.DebtorCode` | Billing debtor exposes company registration number |
| Portal organisation | `identity.OrganisationId` | Identity organisation exposes company registration number |
| Portal person | `identity.UserId` | User exposes organisation + email; CRM contact exposes email + account |
| Modern order | `orders.OrderId` | Order exposes ERP debtor + portal user; shipment and invoice expose order ID |
| Catalogue product | `catalog.ProductSku` | `inventory_sku_map` maps to warehouse, supplier and legacy codes |
| Warehouse product | `warehouse.WarehouseSku` | Inventory mapping table only; not interchangeable with catalogue SKU |
| Supplier product | `supplier.SupplierPartNumber` | Inventory mapping + PO lines |
| Legacy product | `legacy.LegacySku` | Inventory mapping + legacy stock |
| Invoice | `billing.InvoiceNumber` | Invoice exposes debtor + order; finance/payment records expose invoice |
| Payment | `payments.TransactionId` | Payment exposes invoice; payment amount is in minor units |

## No universal identifiers

The model deliberately avoids a universal `CustomerId`, universal product ID, or universal status code. CRM account IDs, ERP debtor codes, identity organisation IDs and Northstar customer IDs are separate Taxi types. Likewise, commercial SKUs, warehouse item codes, supplier part numbers and Northstar part codes are distinct types.

There is no universal migration cross-reference endpoint. The legacy API exposes migration **status**, while actual entity resolution still has to pass through business facts. The finance database also avoids a canonical customer-XREF table.

`MoneyAmount` represents major currency units, while `payments.MinorUnitAmount` represents payment-provider minor units. The conversion endpoint exists so a write workflow must explicitly cross that representation boundary.

## Question levels

The levels on the example questions are a rough guide to how much of the estate a question touches:

- **Level 1–2:** one obvious lookup or a very shallow relationship.
- **Level 3:** identity translation across systems.
- **Level 4:** fan-out or aggregation across several systems.
- **Level 5:** diagnosis or planning, where several facts must be correlated.
- **Level 6:** cross-system writes, sequencing, or mixed read/write planning.

## Main scenario

The principal customer is Northbridge Rail Engineering Ltd. Its deliberately fragmented identity is:

| System | Identifier |
|---|---|
| Legal entity | `08392811` |
| CRM account | `ACC-23918` |
| ERP debtor | `D001922` |
| Portal organisation | `ORG-881` |
| Portal user / Sarah Jones | `U-104` |
| Northstar legacy customer | `NSC-441` |

`ORD-38291` is blocked because line `L-20` (`ACM-7710-X`) cannot be fully allocated at Birmingham under a full-order-ship policy. The product maps to warehouse SKU `7710X` and supplier part `QZ-7710/UK`. Birmingham has no free stock; Rotterdam has 40 free. `PO-90077` is bringing 200 units into Birmingham on 11 August 2026. The shipment only has a label and has not been handed to the carrier.

A coherent decoy customer, Rival Works Ltd, is also used in key lookup stubs. This is intentional: a wrong identifier should be capable of producing a plausible wrong result, not silently return Northbridge data.
