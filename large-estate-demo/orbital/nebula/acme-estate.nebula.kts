// Acme Industrial estate.
// One Ktor HTTP stub + one Postgres test container.
import io.ktor.http.*
import java.math.BigDecimal

// ---------------------------------------------------------------------------
// Order-to-cash reconciliation data.
//
// Northbridge Rail Engineering (debtor D001922) has 16 invoices, Feb-Aug 2026.
// Three systems each record whether an invoice was paid:
//   - ERP billing          : /billing/invoices           (status, major units)
//   - Payment provider     : /payments/payments          (amount_minor, pence)
//   - Finance ledger       : finance_cash_application    (cash applied, major units)
// Most invoices agree across all three. The exceptions are deliberate:
//   INV-88291  ERP says OPEN, but it was paid and the cash is in the ledger (stale ERP status)
//   INV-88014  ERP says OVERDUE + open dispute, but the provider captured it; ledger has no cash
//   INV-87001  Paid £5,120 in full after a £120 credit note - customer is owed £120
//   INV-86377  Provider captured £2,400 of £2,450; ERP closed it as PAID (£50 short)
//   INV-86861  Provider captured the payment twice; ledger applied it once (£1,180.40 to refund)
//   INV-87108  Ledger amount keyed as £4,165.20 instead of £4,615.20 (transposed digits)
//   INV-87690  Ledger posted the cash against INV-87609 (mistyped invoice number)
//   INV-88077  Card payment DECLINED, but the ledger recorded cash and the ERP says PAID
//   INV-88150  EUR invoice paid in GBP - reconciles only after FX conversion (rate 1.1472)
//   INV-88402  Genuinely open and not yet due - no payment expected
// ---------------------------------------------------------------------------
data class NorthbridgeInvoice(
   val invoiceNo: String,
   val orderRef: String,
   val issuedOn: String,
   val grossAmount: String,
   val status: String,
   val currency: String = "GBP",
) {
   val dueOn: String get() = java.time.LocalDate.parse(issuedOn).plusDays(30).toString()
   fun json() = """{"invoice_no":"$invoiceNo","debtor_code":"D001922","order_ref":"$orderRef","issued_on":"$issuedOn","due_on":"$dueOn","gross_amount":$grossAmount,"currency_code":"$currency","status":"$status"}"""
}

data class ProviderTransaction(
   val transactionRef: String,
   val invoiceRef: String,
   val amountMinor: Long,
   val status: String = "CAPTURED",
) {
   fun json() = """{"transaction_ref":"$transactionRef","invoice_ref":"$invoiceRef","amount_minor":$amountMinor,"currency":"GBP","status":"$status","method_id":"PM-72"}"""
}

data class LedgerCashApplication(
   val transactionRef: String,
   val invoiceNo: String,
   val receivedAmount: String,
   val receivedOn: String,
)

val northbridgeInvoices = listOf(
   NorthbridgeInvoice("INV-86012", "ORD-35120", "2026-02-04", "2184.60", "PAID"),
   NorthbridgeInvoice("INV-86140", "ORD-35188", "2026-02-19", "6720.00", "PAID"),
   NorthbridgeInvoice("INV-86377", "ORD-35290", "2026-03-11", "2450.00", "PAID"),
   NorthbridgeInvoice("INV-86505", "ORD-35402", "2026-03-24", "945.30", "PAID"),
   NorthbridgeInvoice("INV-86733", "ORD-35561", "2026-04-09", "3368.75", "PAID"),
   NorthbridgeInvoice("INV-86861", "ORD-35874", "2026-04-22", "1180.40", "PAID"),
   NorthbridgeInvoice("INV-87108", "ORD-36204", "2026-05-06", "4615.20", "PAID"),
   NorthbridgeInvoice("INV-87001", "ORD-36001", "2026-05-20", "5120.00", "PAID"),
   NorthbridgeInvoice("INV-87455", "ORD-36610", "2026-06-02", "5512.40", "PAID"),
   NorthbridgeInvoice("INV-88014", "ORD-37900", "2026-06-15", "8240.00", "OVERDUE"),
   NorthbridgeInvoice("INV-87690", "ORD-36955", "2026-06-17", "2960.00", "PAID"),
   NorthbridgeInvoice("INV-87830", "ORD-37215", "2026-07-01", "1260.00", "PAID"),
   NorthbridgeInvoice("INV-88077", "ORD-37560", "2026-07-15", "1642.75", "PAID"),
   NorthbridgeInvoice("INV-88150", "ORD-37702", "2026-07-24", "3900.00", "PAID", currency = "EUR"),
   NorthbridgeInvoice("INV-88402", "ORD-38377", "2026-08-03", "1975.00", "OPEN"),
   NorthbridgeInvoice("INV-88291", "ORD-38291", "2026-08-06", "3228.50", "OPEN"),
)
val northbridgeInvoicesByNumber = northbridgeInvoices.associateBy { it.invoiceNo }

val providerTransactions = listOf(
   ProviderTransaction("TX-86013", "INV-86012", 218460),
   ProviderTransaction("TX-86141", "INV-86140", 672000),
   ProviderTransaction("TX-86378", "INV-86377", 240000),
   ProviderTransaction("TX-86506", "INV-86505", 94530),
   ProviderTransaction("TX-86734", "INV-86733", 336875),
   ProviderTransaction("TX-86862", "INV-86861", 118040),
   ProviderTransaction("TX-86863", "INV-86861", 118040),
   ProviderTransaction("TX-87109", "INV-87108", 461520),
   ProviderTransaction("TX-87456", "INV-87455", 551240),
   ProviderTransaction("TX-88014", "INV-88014", 824000),
   ProviderTransaction("TX-87691", "INV-87690", 296000),
   ProviderTransaction("TX-87831", "INV-87830", 126000),
   ProviderTransaction("TX-88078", "INV-88077", 164275, status = "DECLINED"),
   ProviderTransaction("TX-88151", "INV-88150", 339958),
   ProviderTransaction("TX-88292", "INV-88291", 322850),
)
val providerTransactionsByRef = providerTransactions.associateBy { it.transactionRef }

val ledgerCashApplications = listOf(
   LedgerCashApplication("TX-86013", "INV-86012", "2184.60", "2026-03-02"),
   LedgerCashApplication("TX-86141", "INV-86140", "6720.00", "2026-03-18"),
   LedgerCashApplication("TX-86378", "INV-86377", "2400.00", "2026-04-08"),
   LedgerCashApplication("TX-86506", "INV-86505", "945.30", "2026-04-21"),
   LedgerCashApplication("TX-86734", "INV-86733", "3368.75", "2026-05-08"),
   LedgerCashApplication("TX-86862", "INV-86861", "1180.40", "2026-05-20"),
   LedgerCashApplication("TX-87109", "INV-87108", "4165.20", "2026-06-03"),
   LedgerCashApplication("TX-81110", "INV-87001", "5120.00", "2026-06-18"),
   LedgerCashApplication("TX-87456", "INV-87455", "5512.40", "2026-06-30"),
   LedgerCashApplication("TX-87691", "INV-87609", "2960.00", "2026-07-15"),
   LedgerCashApplication("TX-87831", "INV-87830", "1260.00", "2026-07-29"),
   LedgerCashApplication("TX-88078", "INV-88077", "1642.75", "2026-08-04"),
   LedgerCashApplication("TX-88151", "INV-88150", "3399.58", "2026-08-05"),
   LedgerCashApplication("TX-88292", "INV-88291", "3228.50", "2026-08-06"),
)


// ---------------------------------------------------------------------------
// Northstar -> Acme core migration data.
//
// Northbridge was migrated off Northstar (legacy customer NSC-441) on 2026-01-15.
// Open Northstar invoices should have been loaded into the ERP as opening balances
// (debtor D001922). What actually happened:
//   NSI-50311, NSI-50487, NSI-50622  Paid before cutover, correctly not migrated
//   NSI-50701  Open, migrated once as OB-00417                     (correct)
//   NSI-50733  Open, never migrated          -> £1,390.00 missing from the ERP
//   NSI-50768  Migrated as OB-00418, then paid into Northstar on 2026-01-28
//                                            -> ERP chases £5,060.00 already paid
//   NSI-50790  Migrated twice (OB-00419, and OB-00431 from a re-run of the load)
//                                            -> £980.40 double-counted
// Truly owed on the Northstar invoices: £5,085.65. ERP opening balances say £9,736.05.
// ---------------------------------------------------------------------------
data class NorthstarInvoice(
   val invoiceNo: String,
   val orderNo: String,
   val invoiceDate: String,
   val totalGbp: String,
   val state: String,
   val paidDate: String? = null,
) {
   fun json(): String {
      val paid = if (paidDate == null) "null" else "\"$paidDate\""
      return """{"inv_no":"$invoiceNo","cust_no":"NSC-441","ord_no":"$orderNo","inv_date":"$invoiceDate","total_gbp":$totalGbp,"state_txt":"$state","paid_date":$paid}"""
   }
}

data class ErpOpeningBalance(
   val reference: String,
   val legacyInvoiceRef: String,
   val migratedOn: String,
   val amount: String,
   val status: String = "OPEN",
) {
   fun json() = """{"ob_ref":"$reference","debtor_code":"D001922","legacy_invoice_ref":"$legacyInvoiceRef","migrated_on":"$migratedOn","amount":$amount,"status":"$status"}"""
}

val northstarInvoices = listOf(
   NorthstarInvoice("NSI-50311", "NO-9790", "2025-08-12", "3140.00", "PAID", paidDate = "2025-09-10"),
   NorthstarInvoice("NSI-50487", "NO-9855", "2025-09-23", "1865.50", "PAID", paidDate = "2025-10-21"),
   NorthstarInvoice("NSI-50622", "NO-9912", "2025-11-19", "4280.00", "PAID", paidDate = "2025-12-18"),
   NorthstarInvoice("NSI-50701", "NO-9940", "2025-12-05", "2715.25", "OPEN"),
   NorthstarInvoice("NSI-50733", "NO-9951", "2025-12-12", "1390.00", "OPEN"),
   NorthstarInvoice("NSI-50768", "NO-9968", "2025-12-19", "5060.00", "PAID", paidDate = "2026-01-28"),
   NorthstarInvoice("NSI-50790", "NO-9977", "2026-01-08", "980.40", "OPEN"),
)
val northstarInvoicesByNumber = northstarInvoices.associateBy { it.invoiceNo }

val erpOpeningBalances = listOf(
   ErpOpeningBalance("OB-00417", "NSI-50701", "2026-01-15", "2715.25"),
   ErpOpeningBalance("OB-00418", "NSI-50768", "2026-01-15", "5060.00"),
   ErpOpeningBalance("OB-00419", "NSI-50790", "2026-01-15", "980.40"),
   ErpOpeningBalance("OB-00431", "NSI-50790", "2026-01-22", "980.40"),
)

stack {
   http {
      get("/crm/accounts/{accountId}") { call ->
         call.respondText(if (call.parameters["accountId"] == "ACC-23918") """{"id":"ACC-23918","display_name":"Northbridge Rail Engineering Ltd","company_no":"08392811","owner_user_id":"USR-17","tier":"GOLD"}""" else """{"id":"ACC-41077","display_name":"Rival Works Ltd","company_no":"09115520","owner_user_id":"USR-41","tier":"SILVER"}""", ContentType.parse("application/json"))
      }
      get("/crm/accounts") { call ->
         call.respondText(if ((call.request.queryParameters["name"] ?: "").contains("Northbridge", ignoreCase = true)) """[{"id":"ACC-23918","display_name":"Northbridge Rail Engineering Ltd","company_no":"08392811","owner_user_id":"USR-17","tier":"GOLD"}]""" else """[{"id":"ACC-41077","display_name":"Rival Works Ltd","company_no":"09115520","owner_user_id":"USR-41","tier":"SILVER"}]""", ContentType.parse("application/json"))
      }
      get("/crm/accounts/by-company/{companyNumber}") { call ->
         call.respondText(if (call.parameters["companyNumber"] == "08392811") """{"id":"ACC-23918","display_name":"Northbridge Rail Engineering Ltd","company_no":"08392811","owner_user_id":"USR-17","tier":"GOLD"}""" else """{"id":"ACC-41077","display_name":"Rival Works Ltd","company_no":"09115520","owner_user_id":"USR-41","tier":"SILVER"}""", ContentType.parse("application/json"))
      }
      get("/crm/accounts/{accountId}/contacts") { call ->
         call.respondText(if (call.parameters["accountId"] == "ACC-23918") """[{"contact_id":"CON-55","account_id":"ACC-23918","full_name":"Sarah Jones","email_address":"sarah.jones@northbridge.example","telephone":"+44 20 7946 0551"}]""" else """[{"contact_id":"CON-91","account_id":"ACC-41077","full_name":"Alex Reed","email_address":"alex.reed@rival.example","telephone":"+44 20 7946 0911"}]""", ContentType.parse("application/json"))
      }
      get("/crm/contacts/by-email") { call ->
         call.respondText(if (call.request.queryParameters["email"] == "sarah.jones@northbridge.example") """{"contact_id":"CON-55","account_id":"ACC-23918","full_name":"Sarah Jones","email_address":"sarah.jones@northbridge.example","telephone":"+44 20 7946 0551"}""" else """{"contact_id":"CON-91","account_id":"ACC-41077","full_name":"Alex Reed","email_address":"alex.reed@rival.example","telephone":"+44 20 7946 0911"}""", ContentType.parse("application/json"))
      }
      get("/crm/contacts") { call ->
         call.respondText(if ((call.request.queryParameters["name"] ?: "").contains("Sarah", ignoreCase = true)) """[{"contact_id":"CON-55","account_id":"ACC-23918","full_name":"Sarah Jones","email_address":"sarah.jones@northbridge.example","telephone":"+44 20 7946 0551"}]""" else """[{"contact_id":"CON-91","account_id":"ACC-41077","full_name":"Alex Reed","email_address":"alex.reed@rival.example","telephone":"+44 20 7946 0911"}]""", ContentType.parse("application/json"))
      }
      get("/crm/accounts/{accountId}/owner") { call ->
         call.respondText(if (call.parameters["accountId"] == "ACC-23918") """{"user_id":"USR-17","name":"Emily Carter","email":"emily.carter@acme.example"}""" else """{"user_id":"USR-41","name":"Marcus Webb","email":"marcus.webb@acme.example"}""", ContentType.parse("application/json"))
      }
      patch("/crm/accounts/{accountId}") { call ->
         call.respondText("""{"id":"${call.parameters["accountId"]}","display_name":"Northbridge Rail Engineering Ltd","company_no":"08392811","owner_user_id":"USR-17","tier":"GOLD"}""", ContentType.parse("application/json"))
      }
      post("/crm/activities") { call ->
         call.respondText("""{"activity_id":"ACT-9001","account_id":"ACC-23918","note":"Activity recorded by stub"}""", ContentType.parse("application/json"))
      }
      get("/identity/organisations/{orgId}") { call ->
         call.respondText(if (call.parameters["orgId"] == "ORG-881") """{"org_id":"ORG-881","legal_name":"Northbridge Rail Engineering Ltd","registered_company":"08392811","primary_domain":"northbridge.example"}""" else """{"org_id":"ORG-410","legal_name":"Rival Works Ltd","registered_company":"09115520","primary_domain":"rival.example"}""", ContentType.parse("application/json"))
      }
      get("/identity/organisations/by-company/{companyNumber}") { call ->
         call.respondText(if (call.parameters["companyNumber"] == "08392811") """{"org_id":"ORG-881","legal_name":"Northbridge Rail Engineering Ltd","registered_company":"08392811","primary_domain":"northbridge.example"}""" else """{"org_id":"ORG-410","legal_name":"Rival Works Ltd","registered_company":"09115520","primary_domain":"rival.example"}""", ContentType.parse("application/json"))
      }
      get("/identity/users/{userId}") { call ->
         call.respondText(if (call.parameters["userId"] == "U-104") """{"user_id":"U-104","org_id":"ORG-881","email":"sarah.jones@northbridge.example","name":"Sarah Jones","role_codes":["BUYER"]}""" else """{"user_id":"U-410","org_id":"ORG-410","email":"alex.reed@rival.example","name":"Alex Reed","role_codes":["BUYER"]}""", ContentType.parse("application/json"))
      }
      get("/identity/users/by-email") { call ->
         call.respondText(if (call.request.queryParameters["email"] == "sarah.jones@northbridge.example") """{"user_id":"U-104","org_id":"ORG-881","email":"sarah.jones@northbridge.example","name":"Sarah Jones","role_codes":["BUYER"]}""" else """{"user_id":"U-410","org_id":"ORG-410","email":"alex.reed@rival.example","name":"Alex Reed","role_codes":["BUYER"]}""", ContentType.parse("application/json"))
      }
      get("/identity/organisations/{orgId}/users") { call ->
         call.respondText(if (call.parameters["orgId"] == "ORG-881") """[{"user_id":"U-104","org_id":"ORG-881","email":"sarah.jones@northbridge.example","name":"Sarah Jones","role_codes":["BUYER"]}]""" else """[{"user_id":"U-410","org_id":"ORG-410","email":"alex.reed@rival.example","name":"Alex Reed","role_codes":["BUYER"]}]""", ContentType.parse("application/json"))
      }
      patch("/identity/users/{userId}/roles") { call ->
         call.respondText("""{"user_id":"${call.parameters["userId"]}","org_id":"ORG-881","email":"sarah.jones@northbridge.example","name":"Sarah Jones","role_codes":["BUYER","ADMIN"]}""", ContentType.parse("application/json"))
      }
      get("/catalog/products/{sku}") { call ->
         call.respondText("""{"sku":"${call.parameters["sku"]}","name":"Industrial Control Relay","ean13":"5012345677710","category_id":"ELEC-CONTROL","mfr_part_no":"MCR-7710","active":true}""", ContentType.parse("application/json"))
      }
      get("/catalog/products") { call ->
         call.respondText("""[{"sku":"ACM-7710-X","name":"Industrial Control Relay","ean13":"5012345677710","category_id":"ELEC-CONTROL","mfr_part_no":"MCR-7710","active":true}]""", ContentType.parse("application/json"))
      }
      get("/catalog/products/by-ean/{ean}") { call ->
         call.respondText("""{"sku":"ACM-7710-X","name":"Industrial Control Relay","ean13":"${call.parameters["ean"]}","category_id":"ELEC-CONTROL","mfr_part_no":"MCR-7710","active":true}""", ContentType.parse("application/json"))
      }
      get("/catalog/products/{sku}/alternates") { call ->
         call.respondText("""[{"source_sku":"${call.parameters["sku"]}","alternate_sku":"ACM-7710-XR","reason":"Approved functionally equivalent relay"}]""", ContentType.parse("application/json"))
      }
      get("/catalog/categories/{categoryId}") { call ->
         call.respondText("""{"category_id":"${call.parameters["categoryId"]}","label":"Electrical control components"}""", ContentType.parse("application/json"))
      }
      get("/catalog/products/{sku}/supplier-links") { call ->
         call.respondText("""[{"sku":"${call.parameters["sku"]}","supplier_id":"SUP-42","supplier_part":"QZ-7710/UK"}]""", ContentType.parse("application/json"))
      }
      post("/catalog/product-equivalents/search") { call ->
         call.respondText("""[{"sku":"ACM-7710-XR","name":"Industrial Control Relay - reinforced","ean13":"5012345677711","category_id":"ELEC-CONTROL","mfr_part_no":"MCR-7710R","active":true}]""", ContentType.parse("application/json"))
      }
      get("/catalog/legacy-code/{legacySku}") { call ->
         call.respondText("""{"legacy_code":"${call.parameters["legacySku"]}","sku":"ACM-7710-X"}""", ContentType.parse("application/json"))
      }
      get("/pricing/list-price/{sku}") { call ->
         call.respondText("""{"sku":"${call.parameters["sku"]}","unit_price":44.78,"currency":"GBP"}""", ContentType.parse("application/json"))
      }
      get("/pricing/customer-price/{debtorCode}/{sku}") { call ->
         call.respondText("""{"debtor_code":"${call.parameters["debtorCode"]}","sku":"${call.parameters["sku"]}","price":41.20,"currency_code":"GBP","source_rule":"PR-77102"}""", ContentType.parse("application/json"))
      }
      post("/pricing/quotes") { call ->
         call.respondText("""{"quote_id":"Q-1001","debtor_code":"D001922","sku":"ACM-7710-X","quantity":200,"net_unit_price":39.95,"currency":"GBP"}""", ContentType.parse("application/json"))
      }
      get("/pricing/discounts/{debtorCode}") { call ->
         call.respondText("""[{"debtor_code":"${call.parameters["debtorCode"]}","category_id":"ELEC-CONTROL","discount_pct":8.0}]""", ContentType.parse("application/json"))
      }
      get("/pricing/contract-price/{agreementId}/{sku}") { call ->
         call.respondText("""{"sku":"${call.parameters["sku"]}","unit_price":41.20,"currency":"GBP"}""", ContentType.parse("application/json"))
      }
      get("/pricing/fx/{pair}") { call ->
         call.respondText("""{"pair":"${call.parameters["pair"]}","rate":1.1472}""", ContentType.parse("application/json"))
      }
      get("/pricing/price-calculations/{agreementId}/{sku}") { call ->
         call.respondText(if (call.parameters["sku"] == "ACM-4400-B") """{"base_amount":18.75,"uplift_pct":4.0,"result_amount":19.50,"currency":"GBP"}""" else """{"base_amount":41.20,"uplift_pct":4.0,"result_amount":42.85,"currency":"GBP"}""", ContentType.parse("application/json"))
      }
      get("/orders/orders/{orderId}") { call ->
         call.respondText(when (call.parameters["orderId"]) { "ORD-38291" -> """{"order_no":"ORD-38291","bill_to":"D001922","ordered_by_user":"U-104","placed_at":"2026-08-05T09:14:22Z","requested_delivery":"2026-08-08","state":"ALLOCATED_PARTIAL","currency":"GBP","gross_total":3228.50}"""; "ORD-37900" -> """{"order_no":"ORD-37900","bill_to":"D001922","ordered_by_user":"U-104","placed_at":"2026-07-14T11:05:00Z","requested_delivery":"2026-07-17","state":"DELIVERED","currency":"GBP","gross_total":1800.00}"""; else -> """{"order_no":"ORD-41001","bill_to":"D004101","ordered_by_user":"U-410","placed_at":"2026-08-03T10:00:00Z","requested_delivery":"2026-08-09","state":"SHIPPED","currency":"GBP","gross_total":910.00}""" }, ContentType.parse("application/json"))
      }
      get("/orders/orders") { call ->
         call.respondText(if (call.request.queryParameters["debtorCode"] == "D001922") """[{"order_no":"ORD-38291","bill_to":"D001922","ordered_by_user":"U-104","placed_at":"2026-08-05T09:14:22Z","requested_delivery":"2026-08-08","state":"ALLOCATED_PARTIAL","currency":"GBP","gross_total":3228.50},{"order_no":"ORD-37900","bill_to":"D001922","ordered_by_user":"U-104","placed_at":"2026-07-14T11:05:00Z","requested_delivery":"2026-07-17","state":"DELIVERED","currency":"GBP","gross_total":1800.00}]""" else """[{"order_no":"ORD-41001","bill_to":"D004101","ordered_by_user":"U-410","placed_at":"2026-08-03T10:00:00Z","requested_delivery":"2026-08-09","state":"SHIPPED","currency":"GBP","gross_total":910.00}]""", ContentType.parse("application/json"))
      }
      get("/orders/orders/by-contact/{userId}") { call ->
         call.respondText(if (call.parameters["userId"] == "U-104") """[{"order_no":"ORD-38291","bill_to":"D001922","ordered_by_user":"U-104","placed_at":"2026-08-05T09:14:22Z","requested_delivery":"2026-08-08","state":"ALLOCATED_PARTIAL","currency":"GBP","gross_total":3228.50}]""" else """[{"order_no":"ORD-41001","bill_to":"D004101","ordered_by_user":"U-410","placed_at":"2026-08-03T10:00:00Z","requested_delivery":"2026-08-09","state":"SHIPPED","currency":"GBP","gross_total":910.00}]""", ContentType.parse("application/json"))
      }
      get("/orders/orders/{orderId}/lines") { call ->
         call.respondText(if (call.parameters["orderId"] == "ORD-38291" || call.parameters["orderId"] == "ORD-37900") """[{"line_ref":"L-10","order_no":"${call.parameters["orderId"]}","catalog_sku":"ACM-4400-B","qty_ordered":10,"qty_cancelled":0,"unit_net":18.75,"line_net":187.50},{"line_ref":"L-20","order_no":"${call.parameters["orderId"]}","catalog_sku":"ACM-7710-X","qty_ordered":60,"qty_cancelled":0,"unit_net":41.20,"line_net":2472.00}]""" else """[{"line_ref":"L-1","order_no":"ORD-41001","catalog_sku":"ACM-9910-Z","qty_ordered":20,"qty_cancelled":0,"unit_net":45.50,"line_net":910.00}]""", ContentType.parse("application/json"))
      }
      get("/orders/orders/{orderId}/status") { call ->
         call.respondText(if (call.parameters["orderId"] == "ORD-38291") """{"order_no":"ORD-38291","state":"ALLOCATED_PARTIAL","hold_reason":"FULL_ORDER_SHIP_POLICY"}""" else """{"order_no":"ORD-41001","state":"SHIPPED","hold_reason":""}""", ContentType.parse("application/json"))
      }
      post("/orders/orders") { call ->
         call.respondText("""{"order_no":"ORD-NEW-1","bill_to":"D001922","ordered_by_user":"U-104","placed_at":"2026-08-07T16:00:00Z","requested_delivery":"2026-08-12","state":"NEW","currency":"GBP","gross_total":0}""", ContentType.parse("application/json"))
      }
      post("/orders/orders/{orderId}/cancel-lines") { call ->
         call.respondText("""{"order_no":"${call.parameters["orderId"]}","state":"PARTIALLY_CANCELLED","hold_reason":""}""", ContentType.parse("application/json"))
      }
      patch("/orders/orders/{orderId}/delivery") { call ->
         call.respondText("""{"order_no":"${call.parameters["orderId"]}","bill_to":"D001922","ordered_by_user":"U-104","placed_at":"2026-08-05T09:14:22Z","requested_delivery":"2026-08-12","state":"ALLOCATED_PARTIAL","currency":"GBP","gross_total":3228.50}""", ContentType.parse("application/json"))
      }
      post("/orders/orders/{orderId}/hold") { call ->
         call.respondText("""{"order_no":"${call.parameters["orderId"]}","state":"ON_HOLD","hold_reason":"MANUAL_HOLD"}""", ContentType.parse("application/json"))
      }
      get("/warehouse/inventory/{warehouseCode}/{warehouseSku}") { call ->
         call.respondText("""{"warehouse":"${call.parameters["warehouseCode"]}","item_code":"${call.parameters["warehouseSku"]}","on_hand":20,"reserved":20,"available":0}""", ContentType.parse("application/json"))
      }
      get("/warehouse/availability/{warehouseSku}") { call ->
         call.respondText(if (call.parameters["warehouseSku"] == "7710X") """[{"warehouse":"BHX","item_code":"7710X","on_hand":20,"reserved":20,"available":0},{"warehouse":"RTM","item_code":"7710X","on_hand":55,"reserved":15,"available":40},{"warehouse":"GLA","item_code":"7710X","on_hand":8,"reserved":8,"available":0}]""" else """[]""", ContentType.parse("application/json"))
      }
      get("/warehouse/reservations") { call ->
         call.respondText(if (call.request.queryParameters["orderId"] == "ORD-38291") """[{"reservation_id":"RES-2001","order_ref":"ORD-38291","line_ref":"L-20","warehouse":"BHX","item_code":"7710X","qty":20,"state":"ALLOCATED"}]""" else """[]""", ContentType.parse("application/json"))
      }
      get("/warehouse/picks") { call ->
         call.respondText(if (call.request.queryParameters["orderId"] == "ORD-38291") """[{"pick_id":"PICK-7781","order_ref":"ORD-38291","warehouse":"BHX","state":"BLOCKED","blocked_reason":"FULL_ORDER_SHIP_POLICY"}]""" else """[]""", ContentType.parse("application/json"))
      }
      post("/warehouse/transfers") { call ->
         call.respondText("""{"transfer_id":"TR-101","item_code":"7710X","from_warehouse":"RTM","to_warehouse":"BHX","qty":40,"state":"REQUESTED"}""", ContentType.parse("application/json"))
      }
      get("/warehouse/transfers/{transferId}") { call ->
         call.respondText("""{"transfer_id":"${call.parameters["transferId"]}","item_code":"7710X","from_warehouse":"RTM","to_warehouse":"BHX","qty":40,"state":"IN_TRANSIT"}""", ContentType.parse("application/json"))
      }
      post("/warehouse/reservations/release") { call ->
         call.respondText("""{"reservation_id":"RES-2001","order_ref":"ORD-38291","line_ref":"L-20","warehouse":"BHX","item_code":"7710X","qty":20,"state":"RELEASED"}""", ContentType.parse("application/json"))
      }
      post("/warehouse/picks/{pickId}/expedite") { call ->
         call.respondText("""{"pick_id":"${call.parameters["pickId"]}","order_ref":"ORD-38291","warehouse":"BHX","state":"EXPEDITED","blocked_reason":""}""", ContentType.parse("application/json"))
      }
      get("/logistics/shipments/by-order/{orderId}") { call ->
         call.respondText(if (call.parameters["orderId"] == "ORD-38291") """[{"shipment_id":"SHP-55009","order_reference":"ORD-38291","tracking_no":"DHLGB882910","origin_depot":"BHX-D1","destination":"Northbridge Rail Engineering, London, UK","status_code":"LABEL_CREATED","eta":"2026-08-10T15:00:00Z"}]""" else """[]""", ContentType.parse("application/json"))
      }
      get("/logistics/shipments/{shipmentId}") { call ->
         call.respondText(if (call.parameters["shipmentId"] == "SHP-55009") """{"shipment_id":"SHP-55009","order_reference":"ORD-38291","tracking_no":"DHLGB882910","origin_depot":"BHX-D1","destination":"Northbridge Rail Engineering, London, UK","status_code":"LABEL_CREATED","eta":"2026-08-10T15:00:00Z"}""" else """{"shipment_id":"SHP-99100","order_reference":"ORD-41001","tracking_no":"DHLGB410010","origin_depot":"RTM-D2","destination":"Rival Works, Leeds, UK","status_code":"IN_TRANSIT","eta":"2026-08-09T12:00:00Z"}""", ContentType.parse("application/json"))
      }
      get("/logistics/tracking/{trackingNumber}") { call ->
         call.respondText(if (call.parameters["trackingNumber"] == "DHLGB882910") """{"tracking_no":"DHLGB882910","shipment_id":"SHP-55009","status":"AWAITING_HANDOVER","last_event_at":"2026-08-06T10:30:00Z","detail":"Carrier label exists; parcel not handed over."}""" else """{"tracking_no":"DHLGB410010","shipment_id":"SHP-99100","status":"IN_TRANSIT","last_event_at":"2026-08-07T14:00:00Z","detail":"Parcel scanned at carrier hub."}""", ContentType.parse("application/json"))
      }
      post("/logistics/estimates") { call ->
         call.respondText("""{"delivery_at":"2026-08-10T15:00:00Z","service":"Road Express"}""", ContentType.parse("application/json"))
      }
      post("/logistics/shipments/{shipmentId}/reroute") { call ->
         call.respondText("""{"shipment_id":"${call.parameters["shipmentId"]}","order_reference":"ORD-38291","tracking_no":"DHLGB882910","origin_depot":"BHX-D1","destination":"Updated destination","status_code":"REROUTE_REQUESTED","eta":"2026-08-11T15:00:00Z"}""", ContentType.parse("application/json"))
      }
      post("/logistics/shipments/{shipmentId}/hold") { call ->
         call.respondText("""{"shipment_id":"${call.parameters["shipmentId"]}","order_reference":"ORD-38291","tracking_no":"DHLGB882910","origin_depot":"BHX-D1","destination":"Northbridge Rail Engineering, London, UK","status_code":"ON_HOLD","eta":"2026-08-11T15:00:00Z"}""", ContentType.parse("application/json"))
      }
      get("/logistics/depots/{depotCode}") { call ->
         call.respondText("""{"depot_code":"${call.parameters["depotCode"]}","address":"Birmingham Logistics Park, UK"}""", ContentType.parse("application/json"))
      }
      get("/procurement/purchase-orders/{poId}") { call ->
         call.respondText(if (call.parameters["poId"] == "PO-90077") """{"po_no":"PO-90077","supplier_id":"SUP-42","deliver_to":"BHX","expected_date":"2026-08-11","status":"CONFIRMED"}""" else """{"po_no":"PO-41010","supplier_id":"SUP-88","deliver_to":"RTM","expected_date":"2026-08-18","status":"CONFIRMED"}""", ContentType.parse("application/json"))
      }
      get("/procurement/purchase-orders") { call ->
         call.respondText(if (call.request.queryParameters["supplierId"] == "SUP-42" || call.request.queryParameters["supplierPart"] == "QZ-7710/UK") """[{"po_no":"PO-90077","supplier_id":"SUP-42","deliver_to":"BHX","expected_date":"2026-08-11","status":"CONFIRMED"}]""" else """[]""", ContentType.parse("application/json"))
      }
      get("/procurement/purchase-orders/{poId}/lines") { call ->
         call.respondText(if (call.parameters["poId"] == "PO-90077") """[{"po_no":"PO-90077","supplier_part":"QZ-7710/UK","qty_ordered":200,"qty_received":0}]""" else """[]""", ContentType.parse("application/json"))
      }
      get("/procurement/inbound/by-warehouse/{warehouseCode}") { call ->
         call.respondText(if (call.parameters["warehouseCode"] == "BHX") """[{"po_no":"PO-90077","supplier_id":"SUP-42","deliver_to":"BHX","expected_date":"2026-08-11","status":"CONFIRMED"}]""" else """[]""", ContentType.parse("application/json"))
      }
      post("/procurement/purchase-orders") { call ->
         call.respondText("""{"po_no":"PO-NEW-1","supplier_id":"SUP-42","deliver_to":"BHX","expected_date":"2026-08-14","status":"DRAFT"}""", ContentType.parse("application/json"))
      }
      patch("/procurement/purchase-orders/{poId}/expedite") { call ->
         call.respondText("""{"po_no":"${call.parameters["poId"]}","supplier_id":"SUP-42","deliver_to":"BHX","expected_date":"2026-08-09","status":"EXPEDITE_REQUESTED"}""", ContentType.parse("application/json"))
      }
      get("/procurement/receipts/by-po/{poId}") { call ->
         call.respondText("""[]""", ContentType.parse("application/json"))
      }
      get("/supplier/suppliers/{supplierId}") { call ->
         call.respondText(if (call.parameters["supplierId"] == "SUP-42") """{"supplier_id":"SUP-42","legal_name":"Quazar Controls GmbH","vat_no":"DE319771042","country":"DE"}""" else """{"supplier_id":"SUP-88","legal_name":"Fenwick Electrics Ltd","vat_no":"GB884100219","country":"GB"}""", ContentType.parse("application/json"))
      }
      get("/supplier/suppliers/by-vat/{vatNumber}") { call ->
         call.respondText("""{"supplier_id":"SUP-42","legal_name":"Quazar Controls GmbH","vat_no":"${call.parameters["vatNumber"]}","country":"DE"}""", ContentType.parse("application/json"))
      }
      get("/supplier/suppliers/{supplierId}/contacts") { call ->
         call.respondText("""[{"supplier_id":"${call.parameters["supplierId"]}","name":"Anika Vogel","email":"anika.vogel@quazar.example"}]""", ContentType.parse("application/json"))
      }
      get("/supplier/suppliers/{supplierId}/catalog/{supplierPart}") { call ->
         call.respondText("""{"supplier_id":"${call.parameters["supplierId"]}","supplier_part":"${call.parameters["supplierPart"]}","description":"24V industrial control relay","min_order_qty":20}""", ContentType.parse("application/json"))
      }
      get("/supplier/lead-times/{supplierId}/{supplierPart}") { call ->
         call.respondText(if (call.parameters["supplierId"] == "SUP-42" && call.parameters["supplierPart"] == "QZ-7710/UK") """{"supplier_id":"SUP-42","supplier_part":"QZ-7710/UK","lead_time_days":7,"as_of":"2026-08-07"}""" else """{"supplier_id":"SUP-88","supplier_part":"XF-99281","lead_time_days":14,"as_of":"2026-08-07"}""", ContentType.parse("application/json"))
      }
      post("/supplier/supplier-products/search") { call ->
         call.respondText("""[{"supplier_id":"SUP-42","supplier_part":"QZ-7710/UK","description":"24V industrial control relay","min_order_qty":20}]""", ContentType.parse("application/json"))
      }
      get("/billing/debtors/{debtorCode}") { call ->
         call.respondText(if (call.parameters["debtorCode"] == "D001922") """{"debtor_code":"D001922","legal_name":"Northbridge Rail Engineering Ltd","registration_no":"08392811","payment_terms":"NET30"}""" else """{"debtor_code":"D004101","legal_name":"Rival Works Ltd","registration_no":"09115520","payment_terms":"NET45"}""", ContentType.parse("application/json"))
      }
      get("/billing/debtors/by-company/{companyNumber}") { call ->
         call.respondText(if (call.parameters["companyNumber"] == "08392811") """{"debtor_code":"D001922","legal_name":"Northbridge Rail Engineering Ltd","registration_no":"08392811","payment_terms":"NET30"}""" else """{"debtor_code":"D004101","legal_name":"Rival Works Ltd","registration_no":"09115520","payment_terms":"NET45"}""", ContentType.parse("application/json"))
      }
      get("/billing/invoices/{invoiceNumber}") { call ->
         val invoice = northbridgeInvoicesByNumber[call.parameters["invoiceNumber"]]
         call.respondText(invoice?.json() ?: """{"invoice_no":"INV-41001","debtor_code":"D004101","order_ref":"ORD-41001","issued_on":"2026-07-02","due_on":"2026-08-16","gross_amount":910.00,"currency_code":"GBP","status":"PAID"}""", ContentType.parse("application/json"))
      }
      get("/billing/invoices") { call ->
         // from / to filter on issued_on (ISO dates compare correctly as strings)
         val from = call.request.queryParameters["from"]
         val to = call.request.queryParameters["to"]
         call.respondText(
            if (call.request.queryParameters["debtorCode"] == "D001922")
               northbridgeInvoices
                  .filter { (from == null || it.issuedOn >= from) && (to == null || it.issuedOn <= to) }
                  .joinToString(",", "[", "]") { it.json() }
            else """[{"invoice_no":"INV-41001","debtor_code":"D004101","order_ref":"ORD-41001","issued_on":"2026-07-02","due_on":"2026-08-16","gross_amount":910.00,"currency_code":"GBP","status":"PAID"}]""",
            ContentType.parse("application/json")
         )
      }
      get("/billing/credit/{debtorCode}") { call ->
         // ERP view of D001922: OVERDUE INV-88014 + OPEN INV-88291 + OPEN INV-88402
         call.respondText(if (call.parameters["debtorCode"] == "D001922") """{"debtor_code":"D001922","limit":50000.00,"outstanding":13443.50,"available":36556.50,"currency":"GBP"}""" else """{"debtor_code":"D004101","limit":15000.00,"outstanding":910.00,"available":14090.00,"currency":"GBP"}""", ContentType.parse("application/json"))
      }
      get("/billing/credit-notes") { call ->
         call.respondText(if (call.request.queryParameters["invoiceNumber"] == "INV-87001") """[{"credit_note_no":"CN-1011","invoice_no":"INV-87001","amount":120.00,"currency":"GBP","reason":"PRICE_ADJUSTMENT"}]""" else """[]""", ContentType.parse("application/json"))
      }
      post("/billing/credit-notes") { call ->
         call.respondText("""{"credit_note_no":"CN-NEW-1","invoice_no":"INV-88291","amount":2472.00,"currency":"GBP","reason":"ORDER_LINE_CANCELLED"}""", ContentType.parse("application/json"))
      }
      get("/billing/opening-balances") { call ->
         call.respondText(
            if (call.request.queryParameters["debtorCode"] == "D001922") erpOpeningBalances.joinToString(",", "[", "]") { it.json() } else "[]",
            ContentType.parse("application/json")
         )
      }
      get("/billing/opening-balances/by-legacy-invoice/{legacyInvoiceNo}") { call ->
         val legacyInvoiceNo = call.parameters["legacyInvoiceNo"]
         call.respondText(
            erpOpeningBalances.filter { it.legacyInvoiceRef == legacyInvoiceNo }.joinToString(",", "[", "]") { it.json() },
            ContentType.parse("application/json")
         )
      }
      get("/payments/payments") { call ->
         val invoiceNumber = call.request.queryParameters["invoiceNumber"]
         call.respondText(
            providerTransactions.filter { it.invoiceRef == invoiceNumber }.joinToString(",", "[", "]") { it.json() },
            ContentType.parse("application/json")
         )
      }
      get("/payments/transactions/{transactionId}") { call ->
         val transactionId = call.parameters["transactionId"]
         call.respondText(
            providerTransactionsByRef[transactionId]?.json()
               ?: """{"transaction_ref":"$transactionId","invoice_ref":"INV-88291","amount_minor":322850,"currency":"GBP","status":"CAPTURED","method_id":"PM-72"}""",
            ContentType.parse("application/json")
         )
      }
      get("/payments/methods/{debtorCode}") { call ->
         call.respondText("""[{"method_id":"PM-72","debtor_code":"${call.parameters["debtorCode"]}","type":"CARD","last4":"1881"}]""", ContentType.parse("application/json"))
      }
      post("/payments/refunds") { call ->
         call.respondText("""{"refund_id":"RF-1009","transaction_ref":"TX-88292","amount_minor":247200,"status":"PENDING"}""", ContentType.parse("application/json"))
      }
      get("/payments/refunds/{refundId}") { call ->
         call.respondText("""{"refund_id":"${call.parameters["refundId"]}","transaction_ref":"TX-88292","amount_minor":247200,"status":"SETTLED"}""", ContentType.parse("application/json"))
      }
      post("/payments/payment-intents") { call ->
         call.respondText("""{"transaction_ref":"TX-NEW-1","invoice_ref":"INV-88291","amount_minor":322850,"currency":"GBP","status":"AUTHORISED","method_id":"PM-72"}""", ContentType.parse("application/json"))
      }
      post("/payments/amount-conversions/to-minor") { call ->
         call.respondText("""{"amount_minor":247200,"currency":"GBP"}""", ContentType.parse("application/json"))
      }
      get("/returns/rmas/{rmaId}") { call ->
         call.respondText("""{"rma_id":"${call.parameters["rmaId"]}","order_ref":"ORD-37900","status":"OPEN","reason":"DAMAGED"}""", ContentType.parse("application/json"))
      }
      get("/returns/rmas") { call ->
         call.respondText("""[]""", ContentType.parse("application/json"))
      }
      get("/returns/eligibility/{orderId}/{lineId}") { call ->
         call.respondText("""{"order_ref":"${call.parameters["orderId"]}","line_ref":"${call.parameters["lineId"]}","eligible":true,"max_qty":10,"reason":"WITHIN_30_DAYS"}""", ContentType.parse("application/json"))
      }
      post("/returns/rmas") { call ->
         call.respondText("""{"rma_id":"RMA-6001","order_ref":"ORD-37900","status":"AUTHORISED","reason":"DAMAGED"}""", ContentType.parse("application/json"))
      }
      post("/returns/rmas/{rmaId}/receive") { call ->
         call.respondText("""{"rma_id":"${call.parameters["rmaId"]}","order_ref":"ORD-37900","status":"RECEIVED","reason":"DAMAGED"}""", ContentType.parse("application/json"))
      }
      post("/returns/rmas/{rmaId}/approve") { call ->
         call.respondText("""{"rma_id":"${call.parameters["rmaId"]}","order_ref":"ORD-37900","approved_amount":82.40,"status":"CREDIT_APPROVED"}""", ContentType.parse("application/json"))
      }
      get("/support/tickets/{ticketId}") { call ->
         call.respondText("""{"ticket_id":"${call.parameters["ticketId"]}","crm_account_id":"ACC-23918","requester_email":"sarah.jones@northbridge.example","subject":"Order ORD-38291 not shipped","status":"OPEN","sla_tier":"GOLD","opened_at":"2026-08-07T08:20:00Z"}""", ContentType.parse("application/json"))
      }
      get("/support/tickets") { call ->
         call.respondText(if (call.request.queryParameters["accountId"] == "ACC-23918") """[{"ticket_id":"TCK-7788","crm_account_id":"ACC-23918","requester_email":"sarah.jones@northbridge.example","subject":"Order ORD-38291 not shipped","status":"OPEN","sla_tier":"GOLD","opened_at":"2026-08-07T08:20:00Z"}]""" else """[]""", ContentType.parse("application/json"))
      }
      get("/support/tickets/by-email") { call ->
         call.respondText(if (call.request.queryParameters["email"] == "sarah.jones@northbridge.example") """[{"ticket_id":"TCK-7788","crm_account_id":"ACC-23918","requester_email":"sarah.jones@northbridge.example","subject":"Order ORD-38291 not shipped","status":"OPEN","sla_tier":"GOLD","opened_at":"2026-08-07T08:20:00Z"}]""" else """[]""", ContentType.parse("application/json"))
      }
      get("/support/slas/{tier}") { call ->
         call.respondText("""{"tier":"${call.parameters["tier"]}","response_minutes":60}""", ContentType.parse("application/json"))
      }
      post("/support/tickets/{ticketId}/comments") { call ->
         call.respondText("""{"ticket_id":"${call.parameters["ticketId"]}","crm_account_id":"ACC-23918","requester_email":"sarah.jones@northbridge.example","subject":"Order ORD-38291 not shipped","status":"OPEN","sla_tier":"GOLD","opened_at":"2026-08-07T08:20:00Z"}""", ContentType.parse("application/json"))
      }
      patch("/support/tickets/{ticketId}") { call ->
         call.respondText("""{"ticket_id":"${call.parameters["ticketId"]}","crm_account_id":"ACC-23918","requester_email":"sarah.jones@northbridge.example","subject":"Order ORD-38291 not shipped","status":"PENDING_CUSTOMER","sla_tier":"GOLD","opened_at":"2026-08-07T08:20:00Z"}""", ContentType.parse("application/json"))
      }
      get("/legacy/customers/{legacyCustomerId}") { call ->
         call.respondText("""{"cust_no":"${call.parameters["legacyCustomerId"]}","company_name":"Northbridge Rail Engineering Ltd","co_reg":"08392811","migrated_debtor":"D001922"}""", ContentType.parse("application/json"))
      }
      get("/legacy/customers/by-company/{companyNumber}") { call ->
         call.respondText(if (call.parameters["companyNumber"] == "08392811") """{"cust_no":"NSC-441","company_name":"Northbridge Rail Engineering Ltd","co_reg":"08392811","migrated_debtor":"D001922"}""" else """{"cust_no":"NSC-900","company_name":"Rival Works Ltd","co_reg":"09115520","migrated_debtor":"D004101"}""", ContentType.parse("application/json"))
      }
      get("/legacy/orders/{legacyOrderId}") { call ->
         call.respondText("""{"legacy_order_no":"${call.parameters["legacyOrderId"]}","cust_no":"NSC-441","ordered_date":"2025-11-19","state_txt":"CLOSED","modern_order_ref":"ORD-35010"}""", ContentType.parse("application/json"))
      }
      get("/legacy/orders") { call ->
         call.respondText(if (call.request.queryParameters["legacyCustomerId"] == "NSC-441") """[{"legacy_order_no":"NO-9912","cust_no":"NSC-441","ordered_date":"2025-11-19","state_txt":"CLOSED","modern_order_ref":"ORD-35010"}]""" else """[]""", ContentType.parse("application/json"))
      }
      get("/legacy/stock/{legacySku}") { call ->
         call.respondText(if (call.parameters["legacySku"] == "NX7710") """{"partcode":"NX7710","free_stock":12,"site":"GLA"}""" else """{"partcode":"${call.parameters["legacySku"]}","free_stock":0,"site":"GLA"}""", ContentType.parse("application/json"))
      }
      post("/legacy/returns") { call ->
         call.respondText("""{"legacy_order_no":"NO-9912","partcode":"NX7710","qty":1,"reason":"DAMAGED"}""", ContentType.parse("application/json"))
      }
      get("/legacy/migration-status/{legacyCustomerId}") { call ->
         call.respondText(if (call.parameters["legacyCustomerId"] == "NSC-441") """{"legacy_customer":"NSC-441","migrated":true,"migrated_at":"2026-01-15T09:00:00Z","target_platform":"ACME_CORE"}""" else """{"legacy_customer":"${call.parameters["legacyCustomerId"]}","migrated":false,"migrated_at":"2026-01-01T00:00:00Z","target_platform":"UNMIGRATED"}""", ContentType.parse("application/json"))
      }
      get("/legacy/invoices") { call ->
         call.respondText(
            if (call.request.queryParameters["legacyCustomerId"] == "NSC-441") northstarInvoices.joinToString(",", "[", "]") { it.json() } else "[]",
            ContentType.parse("application/json")
         )
      }
      get("/legacy/invoices/{legacyInvoiceNo}") { call ->
         val invoice = northstarInvoicesByNumber[call.parameters["legacyInvoiceNo"]]
         if (invoice != null) {
            call.respondText(invoice.json(), ContentType.parse("application/json"))
         } else {
            call.respondText("""{"error":"Northstar invoice not found"}""", ContentType.parse("application/json"), HttpStatusCode.NotFound)
         }
      }
   }

   postgres(imageName = "postgres:16-alpine") {
      table(
         "contracts_agreement", """CREATE TABLE contracts_agreement (                agreement_id varchar(24) NOT NULL,                company_no varchar(16) NOT NULL,                debtor_code varchar(20) NOT NULL,                starts_on date NOT NULL,                ends_on date NOT NULL,                currency varchar(3) NOT NULL,                status varchar(20) NOT NULL,                PRIMARY KEY (agreement_id)            )""",
         data = listOf(
            mapOf("agreement_id" to "AGR-44018", "company_no" to "08392811", "debtor_code" to "D001922", "starts_on" to java.sql.Date.valueOf("2025-08-01"), "ends_on" to java.sql.Date.valueOf("2026-07-31"), "currency" to "GBP", "status" to "EXPIRED"),
            mapOf("agreement_id" to "AGR-44077", "company_no" to "09115520", "debtor_code" to "D004101", "starts_on" to java.sql.Date.valueOf("2026-01-01"), "ends_on" to java.sql.Date.valueOf("2026-12-31"), "currency" to "GBP", "status" to "ACTIVE"),
         )
      )
      table(
         "contracts_price_rule", """CREATE TABLE contracts_price_rule (                rule_id varchar(24) NOT NULL,                agreement_id varchar(24) NOT NULL,                product_sku varchar(32) NOT NULL,                unit_price numeric(12,2) NOT NULL,                discount_pct numeric(6,2) NOT NULL,                PRIMARY KEY (rule_id)            )""",
         data = listOf(
            mapOf("rule_id" to "PR-77101", "agreement_id" to "AGR-44018", "product_sku" to "ACM-4400-B", "unit_price" to BigDecimal("18.75"), "discount_pct" to BigDecimal("6.25")),
            mapOf("rule_id" to "PR-77102", "agreement_id" to "AGR-44018", "product_sku" to "ACM-7710-X", "unit_price" to BigDecimal("41.20"), "discount_pct" to BigDecimal("8.00")),
         )
      )
      table(
         "contracts_entitlement", """CREATE TABLE contracts_entitlement (                agreement_id varchar(24) NOT NULL,                category_id varchar(20) NOT NULL,                max_qty integer NOT NULL,                active_flag boolean NOT NULL,                PRIMARY KEY (agreement_id, category_id)            )""",
         data = listOf(
            mapOf("agreement_id" to "AGR-44018", "category_id" to "ELEC-CONTROL", "max_qty" to 5000, "active_flag" to false),
         )
      )
      table(
         "contracts_renewal_audit", """CREATE TABLE contracts_renewal_audit (                audit_id varchar(24) NOT NULL,                agreement_id varchar(24) NOT NULL,                changed_at timestamp with time zone NOT NULL,                note text,                PRIMARY KEY (audit_id)            )""",
         data = listOf(
            mapOf("audit_id" to "AUD-9001", "agreement_id" to "AGR-44018", "changed_at" to java.sql.Timestamp.from(java.time.Instant.parse("2026-07-15T10:00:00Z")), "note" to "Renewal reminder sent to account owner."),
         )
      )
      table(
         "contracts_account_terms", """CREATE TABLE contracts_account_terms (                debtor_code varchar(20) NOT NULL,                payment_terms varchar(20) NOT NULL,                credit_review_date date NOT NULL,                PRIMARY KEY (debtor_code)            )""",
         data = listOf(
            mapOf("debtor_code" to "D001922", "payment_terms" to "NET30", "credit_review_date" to java.sql.Date.valueOf("2026-09-01")),
         )
      )
      table(
         name = "inventory_sku_map",
         ddl = """CREATE TABLE inventory_sku_map (                product_sku varchar(32) NOT NULL,                warehouse_sku varchar(32) NOT NULL,                supplier_id varchar(20) NOT NULL,                supplier_part varchar(40) NOT NULL,                legacy_sku varchar(30),                PRIMARY KEY (product_sku, warehouse_sku)            )""",
         data = listOf(
            mapOf("product_sku" to "ACM-4400-B", "warehouse_sku" to "4400B", "supplier_id" to "SUP-88", "supplier_part" to "XF-99281", "legacy_sku" to "NS-44B"),
            mapOf("product_sku" to "ACM-7710-X", "warehouse_sku" to "7710X", "supplier_id" to "SUP-42", "supplier_part" to "QZ-7710/UK", "legacy_sku" to "NX7710"),
            mapOf("product_sku" to "ACM-7710-XR", "warehouse_sku" to "7710XR", "supplier_id" to "SUP-42", "supplier_part" to "QZ-7710R/UK", "legacy_sku" to null),
         )
      )
      table(
         "inventory_stock_snapshot", """CREATE TABLE inventory_stock_snapshot (                warehouse_sku varchar(32) NOT NULL,                warehouse_code varchar(12) NOT NULL,                on_hand integer NOT NULL,                reserved_qty integer NOT NULL,                snapshot_at timestamp with time zone NOT NULL,                PRIMARY KEY (warehouse_sku, warehouse_code)            )""",
         data = listOf(
            mapOf("warehouse_sku" to "4400B", "warehouse_code" to "BHX", "on_hand" to 240, "reserved_qty" to 80, "snapshot_at" to java.sql.Timestamp.from(java.time.Instant.parse("2026-08-07T15:00:00Z"))),
            mapOf("warehouse_sku" to "7710X", "warehouse_code" to "BHX", "on_hand" to 20, "reserved_qty" to 20, "snapshot_at" to java.sql.Timestamp.from(java.time.Instant.parse("2026-08-07T15:00:00Z"))),
            mapOf("warehouse_sku" to "7710X", "warehouse_code" to "RTM", "on_hand" to 55, "reserved_qty" to 15, "snapshot_at" to java.sql.Timestamp.from(java.time.Instant.parse("2026-08-07T15:00:00Z"))),
            mapOf("warehouse_sku" to "7710X", "warehouse_code" to "GLA", "on_hand" to 8, "reserved_qty" to 8, "snapshot_at" to java.sql.Timestamp.from(java.time.Instant.parse("2026-08-07T15:00:00Z"))),
         )
      )
      table(
         "inventory_reservation_ledger", """CREATE TABLE inventory_reservation_ledger (                reservation_id varchar(24) NOT NULL,                order_id varchar(24) NOT NULL,                order_line_id varchar(24) NOT NULL,                warehouse_sku varchar(32) NOT NULL,                warehouse_code varchar(12) NOT NULL,                reserved_qty integer NOT NULL,                state varchar(20) NOT NULL,                PRIMARY KEY (reservation_id)            )""",
         data = listOf(
            mapOf("reservation_id" to "RES-2001", "order_id" to "ORD-38291", "order_line_id" to "L-20", "warehouse_sku" to "7710X", "warehouse_code" to "BHX", "reserved_qty" to 20, "state" to "ALLOCATED"),
            mapOf("reservation_id" to "RES-2002", "order_id" to "ORD-38291", "order_line_id" to "L-10", "warehouse_sku" to "4400B", "warehouse_code" to "BHX", "reserved_qty" to 10, "state" to "ALLOCATED"),
         )
      )
      table(
         "inventory_warehouse", """CREATE TABLE inventory_warehouse (                warehouse_code varchar(12) NOT NULL,                name varchar(100) NOT NULL,                address text NOT NULL,                country_code varchar(2) NOT NULL,                PRIMARY KEY (warehouse_code)            )""",
         data = listOf(
            mapOf("warehouse_code" to "BHX", "name" to "Birmingham DC", "address" to "Birmingham, UK", "country_code" to "GB"),
            mapOf("warehouse_code" to "RTM", "name" to "Rotterdam DC", "address" to "Rotterdam, NL", "country_code" to "NL"),
            mapOf("warehouse_code" to "GLA", "name" to "Glasgow DC", "address" to "Glasgow, UK", "country_code" to "GB"),
         )
      )
      table(
         "inventory_cycle_count", """CREATE TABLE inventory_cycle_count (                count_id varchar(24) NOT NULL,                warehouse_sku varchar(32) NOT NULL,                warehouse_code varchar(12) NOT NULL,                counted_qty integer NOT NULL,                counted_at timestamp with time zone NOT NULL,                PRIMARY KEY (count_id)            )""",
         data = listOf(
            mapOf("count_id" to "CC-1001", "warehouse_sku" to "7710X", "warehouse_code" to "BHX", "counted_qty" to 20, "counted_at" to java.sql.Timestamp.from(java.time.Instant.parse("2026-08-07T07:00:00Z"))),
         )
      )
      table(
         "inventory_reorder_policy", """CREATE TABLE inventory_reorder_policy (                warehouse_sku varchar(32) NOT NULL,                warehouse_code varchar(12) NOT NULL,                reorder_point integer NOT NULL,                target_stock integer NOT NULL,                PRIMARY KEY (warehouse_sku, warehouse_code)            )""",
         data = listOf(
            mapOf("warehouse_sku" to "7710X", "warehouse_code" to "BHX", "reorder_point" to 100, "target_stock" to 400),
         )
      )
      table(
         "finance_cash_application", """CREATE TABLE finance_cash_application (                transaction_ref varchar(24) NOT NULL,                invoice_no varchar(24) NOT NULL,                debtor_code varchar(20) NOT NULL,                received_amount numeric(12,2) NOT NULL,                received_on date NOT NULL,                PRIMARY KEY (transaction_ref, invoice_no)            )""",
         data = ledgerCashApplications.map { cash ->
            mapOf("transaction_ref" to cash.transactionRef, "invoice_no" to cash.invoiceNo, "debtor_code" to "D001922", "received_amount" to BigDecimal(cash.receivedAmount), "received_on" to java.sql.Date.valueOf(cash.receivedOn))
         }
      )
      table(
         "finance_payment_terms", """CREATE TABLE finance_payment_terms (                debtor_code varchar(20) NOT NULL,                terms_code varchar(20) NOT NULL,                days_due integer NOT NULL,                PRIMARY KEY (debtor_code)            )""",
         data = listOf(
            mapOf("debtor_code" to "D001922", "terms_code" to "NET30", "days_due" to 30),
         )
      )
      table(
         "finance_credit_review", """CREATE TABLE finance_credit_review (                review_id varchar(24) NOT NULL,                debtor_code varchar(20) NOT NULL,                reviewed_on date NOT NULL,                risk_score numeric(5,2) NOT NULL,                note text,                PRIMARY KEY (review_id)            )""",
         data = listOf(
            mapOf("review_id" to "CR-991", "debtor_code" to "D001922", "reviewed_on" to java.sql.Date.valueOf("2026-07-20"), "risk_score" to BigDecimal("31.50"), "note" to "Stable account; one invoice disputed."),
         )
      )
      table(
         "finance_dispute", """CREATE TABLE finance_dispute (                dispute_id varchar(24) NOT NULL,                invoice_no varchar(24) NOT NULL,                opened_on date NOT NULL,                amount numeric(12,2) NOT NULL,                status varchar(20) NOT NULL,                PRIMARY KEY (dispute_id)            )""",
         data = listOf(
            mapOf("dispute_id" to "DSP-73", "invoice_no" to "INV-88014", "opened_on" to java.sql.Date.valueOf("2026-07-30"), "amount" to BigDecimal("8240.00"), "status" to "OPEN"),
         )
      )
      table(
         "finance_credit_note_ledger", """CREATE TABLE finance_credit_note_ledger (                credit_note_no varchar(24) NOT NULL,                invoice_no varchar(24) NOT NULL,                issued_on date NOT NULL,                amount numeric(12,2) NOT NULL,                currency varchar(3) NOT NULL,                PRIMARY KEY (credit_note_no)            )""",
         data = listOf(
            mapOf("credit_note_no" to "CN-1011", "invoice_no" to "INV-87001", "issued_on" to java.sql.Date.valueOf("2026-06-10"), "amount" to BigDecimal("120.00"), "currency" to "GBP"),
         )
      )
      table(
         "analytics_customer_signal", """CREATE TABLE analytics_customer_signal (                signal_id varchar(24) NOT NULL,                company_no varchar(16) NOT NULL,                signal_type varchar(40) NOT NULL,                score numeric(5,2) NOT NULL,                observed_at timestamp with time zone NOT NULL,                PRIMARY KEY (signal_id)            )""",
         data = listOf(
            mapOf("signal_id" to "SIG-201", "company_no" to "08392811", "signal_type" to "ORDER_DELAY_RATE", "score" to BigDecimal("72.00"), "observed_at" to java.sql.Timestamp.from(java.time.Instant.parse("2026-08-06T23:00:00Z"))),
            mapOf("signal_id" to "SIG-202", "company_no" to "08392811", "signal_type" to "PAYMENT_RISK", "score" to BigDecimal("31.50"), "observed_at" to java.sql.Timestamp.from(java.time.Instant.parse("2026-08-06T23:00:00Z"))),
         )
      )
      table(
         "analytics_order_event", """CREATE TABLE analytics_order_event (                event_id varchar(24) NOT NULL,                order_id varchar(24) NOT NULL,                event_type varchar(40) NOT NULL,                event_at timestamp with time zone NOT NULL,                detail text,                PRIMARY KEY (event_id)            )""",
         data = listOf(
            mapOf("event_id" to "EVT-1", "order_id" to "ORD-38291", "event_type" to "ALLOCATED_PARTIAL", "event_at" to java.sql.Timestamp.from(java.time.Instant.parse("2026-08-06T10:11:00Z")), "detail" to "Line L-20 only partially allocatable at BHX."),
            mapOf("event_id" to "EVT-2", "order_id" to "ORD-38291", "event_type" to "PICK_BLOCKED", "event_at" to java.sql.Timestamp.from(java.time.Instant.parse("2026-08-06T10:20:00Z")), "detail" to "Pick held until full-order policy satisfied."),
         )
      )
      table(
         "analytics_service_case_signal", """CREATE TABLE analytics_service_case_signal (                signal_id varchar(24) NOT NULL,                ticket_id varchar(24) NOT NULL,                order_id varchar(24),                severity numeric(5,2) NOT NULL,                PRIMARY KEY (signal_id)            )""",
         data = listOf(
            mapOf("signal_id" to "SIG-T1", "ticket_id" to "TCK-7788", "order_id" to "ORD-38291", "severity" to BigDecimal("80.00")),
         )
      )
      table(
         "analytics_supplier_performance", """CREATE TABLE analytics_supplier_performance (                supplier_id varchar(20) NOT NULL,                supplier_part varchar(40) NOT NULL,                otif_pct numeric(5,2) NOT NULL,                avg_delay_days numeric(5,2) NOT NULL,                PRIMARY KEY (supplier_id, supplier_part)            )""",
         data = listOf(
            mapOf("supplier_id" to "SUP-42", "supplier_part" to "QZ-7710/UK", "otif_pct" to BigDecimal("71.00"), "avg_delay_days" to BigDecimal("3.40")),
         )
      )
      table(
         "analytics_product_demand", """CREATE TABLE analytics_product_demand (                product_sku varchar(32) NOT NULL,                warehouse_code varchar(12) NOT NULL,                weekly_demand integer NOT NULL,                snapshot_date date NOT NULL,                PRIMARY KEY (product_sku, warehouse_code)            )""",
         data = listOf(
            mapOf("product_sku" to "ACM-7710-X", "warehouse_code" to "BHX", "weekly_demand" to 155, "snapshot_date" to java.sql.Date.valueOf("2026-08-03")),
         )
      )
      table(
         "analytics_account_health", """CREATE TABLE analytics_account_health (                company_no varchar(16) NOT NULL,                health_score numeric(5,2) NOT NULL,                calculated_at timestamp with time zone NOT NULL,                explanation text,                PRIMARY KEY (company_no)            )""",
         data = listOf(
            mapOf("company_no" to "08392811", "health_score" to BigDecimal("63.00"), "calculated_at" to java.sql.Timestamp.from(java.time.Instant.parse("2026-08-07T02:00:00Z")), "explanation" to "Operational delays elevated; payment profile acceptable."),
         )
      )
   }
}
