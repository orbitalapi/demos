import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.orbitalhq.nebula.io.protobuf.protobufSchema
import com.orbitalhq.nebula.utils.duration
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import kotlin.random.Random
import com.orbitalhq.nebula.kafka.MessageSerializer



// Enum for RewardsStatus
enum class RewardsStatus {
   Gold, Silver, Bronze
}


// Data class for Customer
data class Customer(val id: String, val name: String?, val email: String, val countryCode: String)

/**
 * This stack stands up:
 *  - A Kafka broker that emits:
 *    - Food order events
 *    - Delivery update events
 *
 *  - An HTTP server that provides customer and restaurant data
 *
 */
stack {
   val restaurants = listOf(
      mapOf(
         "id" to "REST-1",
         "name" to "Pizza Palace",
         "cuisineType" to "Italian",
         "latitude" to 40.7128,
         "longitude" to -74.0060
      ),
      mapOf(
         "id" to "REST-2",
         "name" to "Sushi Express",
         "cuisineType" to "Japanese",
         "latitude" to 40.7589,
         "longitude" to -73.9851
      )
   )
   val firstNames = listOf(
      "Alice",
      "Bob",
      "Charlie",
      "Diana",
      "Edward",
      "Fiona",
      "George",
      "Hannah",
      "Ian",
      "Julia",
      "Kevin",
      "Laura",
      "Michael",
      "Nina",
      "Oliver"
   )
   val lastNames = listOf(
      "Smith",
      "Johnson",
      "Williams",
      "Brown",
      "Jones",
      "Miller",
      "Davis",
      "Garcia",
      "Martinez",
      "Hernandez",
      "Lopez",
      "Gonzalez",
      "Wilson",
      "Anderson",
      "Thomas"
   )
   val countries = listOf("NZ", "AU", "UK", "US")
   val customers = (0..50).mapIndexed { index, _ ->
      val firstName = firstNames.random()
      val lastName = lastNames.random()
      val name = if (index == 1) {
         null
      } else "$firstName $lastName"
      Customer(
         "CUST-$index",
         name,
         "${firstName.substring(0..0).lowercase()}.${lastName.lowercase()}@example.com",
         countries.random()
      )
   }.associateBy { it.id }


   http {
      val mapper = jacksonObjectMapper()
      get("/customers/{id}") { call ->
         val id = call.parameters["id"]!!
         if (id == "CUST-3") {
            error("Not found")
         }
         call.respondText(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(customers[id]!!))
      }
   }
   kafka {
      val counter = AtomicInteger(0)

      data class DeliveryState(
         val orderId: String,
         val deliveryId: String,
         val driverId: String,
         var currentStatus: Int = 0,
         val statuses: List<String> = listOf(
            "DRIVER_ASSIGNED",
            "PICKED_UP",
            "IN_TRANSIT",
            "DELIVERED"
         )
      )

      val activeDeliveries = ConcurrentHashMap<String, DeliveryState>()

      // Generate new orders
      producer("750ms".duration(), "orders") {
         jsonMessage {
            val orderId = "ORD-${counter.incrementAndGet()}"
            println("Creating order $orderId...")
            val restaurant = restaurants.random()
            val items = listOf(
               mapOf(
                  "name" to "Main Dish",
                  "quantity" to Random.nextInt(1, 3),
                  "price" to Random.nextDouble(10.0, 30.0).toBigDecimal()
                     .setScale(2, RoundingMode.HALF_UP)
               ),
               mapOf(
                  "name" to "Side Dish",
                  "quantity" to Random.nextInt(1, 2),
                  "price" to Random.nextDouble(5.0, 15.0).toBigDecimal()
                     .setScale(2, RoundingMode.HALF_UP)
               )
            )

            val customerId = "CUST-${Random.nextInt(1, 4)}".let { if (it == "CUST-1") null else it }
            val order = mapOf(
               "orderId" to orderId,
               "restaurantId" to restaurant["id"],
               "customerId" to "CUST-${Random.nextInt(1, 4)}",
               "items" to items,
               "totalAmount" to items.sumOf {
                  ((it["price"] as BigDecimal) * (it["quantity"] as Int).toBigDecimal()).setScale(
                     2,
                     RoundingMode.HALF_UP
                  )
               },
               "orderTime" to Instant.now().toString()
            )

            val deliveryId = "DEL-${counter.incrementAndGet()}"
            val driverId = "DRV-${Random.nextInt(1, 20)}"
            activeDeliveries[orderId] = DeliveryState(orderId, deliveryId, driverId)

            order
         }


      }

      // Generate sequential delivery updates
      producer("350ms".duration(), "deliveries") {
         jsonMessages {
            val messages = activeDeliveries.entries.mapNotNull { (orderId, state) ->
               if (state.currentStatus < state.statuses.size) {
                  val delivery = mapOf(
                     "deliveryId" to state.deliveryId,
                     "orderId" to orderId,
                     "driverId" to state.driverId,
                     "status" to state.statuses[state.currentStatus],
                     "timestamp" to Instant.now().toString(),
                     "currentLocation" to mapOf(
                        "latitude" to (40.7128 + Random.nextDouble(-0.1, 0.1)),
                        "longitude" to (-74.0060 + Random.nextDouble(-0.1, 0.1))
                     )
                  )

                  state.currentStatus++
                  if (state.currentStatus >= state.statuses.size) {
                     activeDeliveries.remove(orderId)
                  }
                  delivery
               } else {
                  null
               }
            }
            messages
         }
      }
   }

   mongo(databaseName = "orbit-eats") {
      collection("order-updates")
   }

   hazelcast { }
}
