import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.random.Random

data class Warehouse(
   val name: String,
   val virtualId: Long,
   val physicalId: Long,
   val digitalId: String,
   val fulfillmentCenter: String,
   val countryCode: String,
   val channelId: Int,
   val acceptsReturns: Boolean
)


val warehouseTestData = listOf(
   Warehouse(
      name = "UK-LONDON-01",
      virtualId = 10001L,
      physicalId = 1001L,
      digitalId = "D1001",
      fulfillmentCenter = "London Heathrow Hub",
      countryCode = "GB",
      channelId = 1,
      acceptsReturns = true
   ),
   Warehouse(
      name = "UK-MANCHESTER-02",
      virtualId = 10002L,
      physicalId = 1002L,
      digitalId = "D1002",
      fulfillmentCenter = "Manchester Distribution Center",
      countryCode = "GB",
      channelId = 1,
      acceptsReturns = true
   ),
   Warehouse(
      name = "DE-BERLIN-01",
      virtualId = 20001L,
      physicalId = 2001L,
      digitalId = "D2001",
      fulfillmentCenter = "Berlin Logistics Hub",
      countryCode = "DE",
      channelId = 2,
      acceptsReturns = true
   ),
   Warehouse(
      name = "FR-PARIS-01",
      virtualId = 30001L,
      physicalId = 3001L,
      digitalId = "D3001",
      fulfillmentCenter = "Paris Distribution Center",
      countryCode = "FR",
      channelId = 3,
      acceptsReturns = false
   ),
   Warehouse(
      name = "ES-MADRID-01",
      virtualId = 40001L,
      physicalId = 4001L,
      digitalId = "D4001",
      fulfillmentCenter = "Madrid Logistics Center",
      countryCode = "ES",
      channelId = 4,
      acceptsReturns = true
   )
)

class WarehouseDataGenerator {
   private val categories =
      listOf("Dresses", "Tops", "Bottoms", "Shoes", "Accessories", "Swimwear", "Activewear", "Outerwear")
   private val conditions = listOf("NEW", "LIKE_NEW", "GOOD", "FAIR")
   private val suppliers = listOf("FashionCo", "StyleWorks", "TrendSetters", "LuxuryBrands", "UrbanChic")
   private val shippingMethods = listOf("STANDARD", "EXPRESS", "NEXT_DAY", "SAME_DAY")
   private val dateFormatter = DateTimeFormatter.ISO_DATE_TIME

   private val productAdjectives =
      listOf("Elegant", "Classic", "Modern", "Vintage", "Luxurious", "Casual", "Trendy", "Chic")
   private val productNouns = listOf("Shirt", "Dress", "Jeans", "Skirt", "Blazer", "Coat", "Sweater", "Boots")
   private val brandPrefixes = listOf("Royal", "Urban", "Elite", "Prime", "Classic", "Modern", "Premium")
   private val brandSuffixes = listOf("Fashion", "Style", "Wear", "Collection", "Apparel", "Designs")

   fun generateData(rowCount: Long): Sequence<String> = sequence {
      // Emit header first
      yield(generateHeader() + "\n")

      // Then emit each row
      var rowsGenerated = 0L
      while (rowsGenerated < rowCount) {
         yield(generateRow() + "\n")
         rowsGenerated++
      }
   }


   private fun generateHeader(): String = listOf(
      "item_id",
      "sku",
      "timestamp",
      "warehouse_id",
      "zone_id",
      "rack_id",
      "bin_id",
      "product_name",
      "brand_name",
      "category",
      "subcategory",
      "supplier_id",
      "supplier_name",
      "condition",
      "quantity_available",
      "quantity_reserved",
      "quantity_damaged",
      "reorder_point",
      "restock_level",
      "unit_cost",
      "retail_price",
      "currency",
      "weight_kg",
      "length_cm",
      "width_cm",
      "height_cm",
      "last_inventory_check",
      "next_inventory_check",
      "shipping_method",
      "handling_instructions"
   ).joinToString(",")

   private fun generateRow(): String {
      val itemId = Random.nextInt(10000000, 99999999).toString()
      val timestamp = LocalDateTime.now().minusMinutes(Random.nextLong(0, 1440))
      val warehouseId = warehouseTestData.random().virtualId
      val zoneId = "${('A'..'F').random()}${(1..4).random()}"
      val category = categories.random()
      val condition = conditions.random()
      val supplier = suppliers.random()

      return listOf(
         itemId,
         "SKU-${Random.nextInt(100000, 999999)}",
         timestamp.format(dateFormatter),
         warehouseId,
         zoneId,
         "${zoneId}-R${(1..20).random().toString().padStart(2, '0')}",
         "${zoneId}-B${(1..50).random().toString().padStart(3, '0')}",
         generateProductName(),
         generateBrandName(),
         category,
         "$category-${productNouns.random()}",
         "SUP-${Random.nextInt(10000, 99999)}",
         supplier,
         condition,
         (1..100).random(),
         (0..10).random(),
         (0..5).random(),
         20,
         50,
         String.format("%.2f", Random.nextDouble(5.0, 200.0)),
         String.format("%.2f", Random.nextDouble(10.0, 500.0)),
         "GBP",
         String.format("%.3f", Random.nextDouble(0.1, 10.0)),
         String.format("%.1f", Random.nextDouble(10.0, 100.0)),
         String.format("%.1f", Random.nextDouble(10.0, 100.0)),
         String.format("%.1f", Random.nextDouble(10.0, 100.0)),
         timestamp.minusDays(Random.nextLong(1, 30)).format(dateFormatter),
         timestamp.plusDays(Random.nextLong(1, 30)).format(dateFormatter),
         shippingMethods.random(),
         generateHandlingInstructions()
      ).joinToString(",")
   }

   private fun generateProductName(): String {
      val adjective = productAdjectives.random()
      val noun = productNouns.random()
      return "$adjective $noun"
   }

   private fun generateBrandName(): String {
      val prefix = brandPrefixes.random()
      val suffix = brandSuffixes.random()
      return "$prefix $suffix"
   }

   private fun generateHandlingInstructions(): String {
      val instructions = listOf(
         "Handle with care",
         "Fragile",
         "This side up",
         "Keep dry",
         "Temperature sensitive",
         "Stack max 3",
         "No special handling required"
      )
      return instructions.random().replace(",", "-")
   }
}

stack {
   kafka { }


   http {
      get("/warehouses/{virtualId}") { call ->
         val virtualId = call.parameters["virtualId"]
         println("Virtual ID: $virtualId")
         val warehouse = warehouseTestData.first { it.virtualId.toString() == virtualId }
         val mapper = jacksonObjectMapper()
         val response = mapOf(
            "virtualId" to warehouse.virtualId,
            "physicalId" to warehouse.physicalId,
            "fulfillmentCenter" to warehouse.fulfillmentCenter,
            "acceptsReturns" to warehouse.acceptsReturns
         )
         call.respondText(mapper.writeValueAsString(response))
      }
   }

   postgres(componentName = "warehouses-db") {
      table(
         "digital_warehouse", """
         CREATE TABLE digital_warehouse (
            physical_id INT PRIMARY KEY,
            digital_id VARCHAR NOT NULL,
            channel_id INT NOT NULL,
            country_code VARCHAR NOT NULL
         )
      """.trimIndent(), data = warehouseTestData.map {
            mapOf(
               "physical_id" to it.physicalId,
               "digital_id" to it.digitalId,
               "channel_id" to it.channelId,
               "country_code" to it.countryCode
            )
         })
   }

   s3 {
      val sequence = WarehouseDataGenerator().generateData(1_000_000)
      bucket("warehouse-reports") {
         file("warehouse-report-001.csv", content = sequence)
      }
   }


   mongo(databaseName = "warehouseDb") {
      collection("inventoryReports")
   }
}
