import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.orbitalhq.nebula.utils.duration
import java.math.BigDecimal
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import kotlin.random.Random

// Enum for RewardsStatus
enum class RewardsStatus {
   Gold, Silver, Bronze
}

// Data class for Customer
data class Customer(val id: String, val name: String, val email: String, val rewardsStatus: RewardsStatus)

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
   val customers = (0..50).mapIndexed { index, _ ->
      val firstName = firstNames.random()
      val lastName = lastNames.random()
      Customer(
         "CUST-$index",
         "$firstName $lastName",
         "${firstName.substring(0..0).lowercase()}.${lastName.lowercase()}@example.com",
         RewardsStatus.entries.random()
      )
   }.associateBy { it.id }

   http {
      val mapper = jacksonObjectMapper()
      get("/customers/{id}") { call ->
         val id = call.parameters["id"]!!

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
      producer("2000ms".duration(), "orders") {
         jsonMessage {
            println("Creating new order...")
            val orderId = "ORD-${counter.incrementAndGet()}"
            val restaurant = restaurants.random()
            val items = listOf(
               mapOf(
                  "name" to "Main Dish",
                  "quantity" to Random.nextInt(1, 3),
                  "price" to Random.nextDouble(10.0, 30.0).toBigDecimal()
               ),
               mapOf(
                  "name" to "Side Dish",
                  "quantity" to Random.nextInt(1, 2),
                  "price" to Random.nextDouble(5.0, 15.0).toBigDecimal()
               )
            )

            val order = mapOf(
               "orderId" to orderId,
               "restaurantId" to restaurant["id"],
               "customerId" to "CUST-${Random.nextInt(1, 50)}",
               "items" to items,
               "totalAmount" to items.sumOf {
                  (it["price"] as BigDecimal) * (it["quantity"] as Int).toBigDecimal()
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
      producer("500ms".duration(), "deliveries") {
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
}
