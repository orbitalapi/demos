import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.orbitalhq.nebula.utils.duration
import io.ktor.http.*

enum class DeliveryMismatch(val updateQuantity: (Int) -> Int) {
   CORRECT_QTY( { it }),
   OVER({ it + Random.nextInt(0,10)}),
   UNDER({ it - Random.nextInt(0,10)})
}

stack {
   // Product catalog with detailed product information
   data class ProductDetails(
      val productId: String,
      val sku: String,
      val productName: String,
      val category: String,
      val storageLocation: String
   )

   val productCatalog = listOf(
      ProductDetails("PROD-1001", "SKU-LAP001", "Gaming Laptop Pro 15\"", "Electronics", "WAREHOUSE-A-02"),
      ProductDetails("PROD-1002", "SKU-MON002", "4K Monitor Ultra 27\"", "Electronics", "WAREHOUSE-A-03"),
      ProductDetails("PROD-1003", "SKU-KEY003", "Mechanical Keyboard RGB", "Accessories", "WAREHOUSE-B-01"),
      ProductDetails("PROD-1004", "SKU-MOU004", "Wireless Gaming Mouse", "Accessories", "WAREHOUSE-B-01"),
      ProductDetails("PROD-1005", "SKU-HEA005", "Noise Cancelling Headphones", "Audio", "WAREHOUSE-B-02"),
      ProductDetails("PROD-1006", "SKU-TAB006", "Tablet Pro 12-inch", "Electronics", "WAREHOUSE-A-01"),
      ProductDetails("PROD-1007", "SKU-SMA007", "Smartphone X Plus", "Electronics", "WAREHOUSE-A-01"),
      ProductDetails("PROD-1008", "SKU-SPE008", "Bluetooth Speaker Premium", "Audio", "WAREHOUSE-B-02"),
      ProductDetails("PROD-1009", "SKU-CAM009", "Digital Camera 24MP", "Photography", "WAREHOUSE-C-01"),
      ProductDetails("PROD-1010", "SKU-CHA010", "Fast Charging Cable USB-C", "Accessories", "WAREHOUSE-B-03"),
      ProductDetails("PROD-1011", "SKU-HDD011", "External Hard Drive 2TB", "Storage", "WAREHOUSE-C-02"),
      ProductDetails("PROD-1012", "SKU-POW012", "Power Bank 20000mAh", "Accessories", "WAREHOUSE-B-03"),
      ProductDetails("PROD-1013", "SKU-WEB013", "HD Webcam with Microphone", "Electronics", "WAREHOUSE-A-02"),
      ProductDetails("PROD-1014", "SKU-ROU014", "WiFi Router AC1900", "Networking", "WAREHOUSE-C-01"),
      ProductDetails("PROD-1015", "SKU-SSD015", "SSD Drive 1TB NVMe", "Storage", "WAREHOUSE-C-02")
   )

   data class StockLevels(val productId: String, val currentStock: Int)
   val stockLevels = mutableMapOf<String,StockLevels>(
      *productCatalog.map { it.productId to StockLevels(it.productId, 0) }.toTypedArray()
   )

   data class OrderDetails(
      val orderId: String,
      val product: ProductDetails,
      val orderedQuantity: Int,
      val deliveredQuantity: Int,
      val supplierId: String,
      val deliveryRef: String
   )
   val orders = mutableListOf<OrderDetails>()

   fun createOrder(supplier: String): OrderDetails {
      val product = productCatalog.random()
      val quantity = Random.nextInt(1,100)
      val deliveredQuantity = DeliveryMismatch.entries.random()
         .updateQuantity(quantity)
      val deliveryRef = java.util.UUID.randomUUID().toString()
      val orderId = java.util.UUID.randomUUID().toString()
      val order = OrderDetails(
         orderId,
         product,
         quantity,
         deliveredQuantity,
         supplier,
         deliveryRef
      )
      orders.add(order)
      return order
   }

   data class UpdateStockDeliveryRequest(
      val productId: String,
      val productName: String,
      val receivedQuantity: Int
   )
   http {
      post("/deliveries") { call ->
         //
         val requestPayload = call.receiveText()
         println("Request receivied - Body: \n ${requestPayload}")

         val request = jacksonObjectMapper().readValue<UpdateStockDeliveryRequest>(requestPayload)
//         val request = UpdateStockDeliveryRequest(
//            map["productId"] as String,
//            map["productName"] as String,
//            (map["receivedQuantity"] as String).toInt()
//         )
         val productStock = stockLevels[request.productId]!!
         val updated = productStock.copy(currentStock = productStock.currentStock + request.receivedQuantity)
         stockLevels[request.productId] = updated
         val response = jacksonObjectMapper().writeValueAsString(mapOf(
            "productId" to request.productId,
            "productName" to request.productName,
            "receivedQuantity" to request.receivedQuantity,
            "updatedQuantity" to updated.currentStock
         ))
         call.respondText(response)
      }

      get("/orders/{orderId}") { call ->
         val orderId = call.parameters["orderId"]
         val order = orders.find { it.orderId == orderId }
         if (order == null) {
            call.respondText("""{ "error" : "Order with id '$orderId' not found" }""", status = HttpStatusCode.NotFound)
            return@get
         }
         val json = jacksonObjectMapper().writeValueAsString(order)
         call.respondText(json)

      }
      get("/deliveries/{deliveryId}/order") { call ->
         val deliveryId = call.parameters["deliveryId"]
         val order = orders.find { it.deliveryRef == deliveryId }
         if (order == null) {
            call.respondText("""{ "error" : "Order with deliveryId '$deliveryId' not found" }""", status = HttpStatusCode.NotFound)
            return@get
         }
         val json = jacksonObjectMapper().writeValueAsString(mapOf(
            "orderId" to order!!.orderId
         ))
         call.respondText(json)
      }

      // ProductsApi - getProductId endpoint
      get("/products/lookup/sku/{sku}") { call ->
         val sku = call.parameters["sku"]
         if (sku == null) {
            call.respondText("""{ "error" :"SKU is required" }""", status = HttpStatusCode.BadRequest)
            return@get
         }
         println("Lookup by SKU : $sku")

         val product = productCatalog.find { it.sku == sku }
         if (product == null) {
            call.respondText("""{ "error" : "Product with SKU '$sku' not found" }""", status = HttpStatusCode.NotFound)
            return@get
         }

         val response = mapOf(
            "productId" to product.productId,
            "sku" to product.sku
         )
         val json = jacksonObjectMapper().writeValueAsString(response)
         call.respondText(json)
      }

      // ProductsApi - getProduct endpoint
      get("/products/{productId}") { call ->
         val productId = call.parameters["productId"]
         if (productId == null) {
            call.respondText("""{ "error" : "Product ID parameter is required }""", status = HttpStatusCode.BadRequest)
            return@get
         }

         val product = productCatalog.find { it.productId == productId }
         if (product == null) {
            call.respondText("""{ "error" : "Product with ID '$productId' not found" }""", status = HttpStatusCode.NotFound)
            return@get
         }

         val response = mapOf(
            "productId" to product.productId,
            "sku" to product.sku,
            "productName" to product.productName,
            "category" to product.category,
            "storageLocation" to product.storageLocation
         )
         val json = jacksonObjectMapper().writeValueAsString(response)
         call.respondText(json)
      }
   }
   kafka {
      // FastShip delivery events - emits every 5 seconds
      producer("2s".duration(), "fastship-deliveries") {
         jsonMessage {
            val order = createOrder("FASTSHIP")
            mapOf(
               "sku" to order.product.sku,
               "quantity" to order.deliveredQuantity,
               "deliveryRef" to order.deliveryRef,
               "supplierId" to order.supplierId,
               "dateDelivered" to java.time.LocalDateTime.now().toString()
            )
         }
      }

      // SurePost delivery events - emits every 5 seconds
      producer("2s".duration(), "surepost-deliveries") {
         jsonMessage {
            val order = createOrder("SUREPOST")
            mapOf(
               "productId" to order.product.productId,
               "qtyDelivered" to order.deliveredQuantity,
               "orderId" to order.orderId,
               "supplierId" to order.supplierId,
               "timestamp" to java.time.format.DateTimeFormatter
                  .ofPattern("d-MMM-yyyy HH:mm:ss", java.util.Locale.ENGLISH)
                  .format(java.time.LocalDateTime.now())
            )
         }
      }
   }
}
