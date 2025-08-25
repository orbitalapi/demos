import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay
stack {
   mongo(databaseName = "customers") {
      collection(name = "category", data = listOf(
         mapOf("_id" to "gold", "description" to "Gold Tier"),
         mapOf("_id" to "silver", "description" to "Silver Tier"),
         mapOf("_id" to "bronze", "description" to "Bronze Tier")
      ))
   }
   http {
      get("/") { applicationCall ->
         delay(2000) // Delay for 2000ms, to intentionally slow down the processing
         applicationCall.respondText("""{ "hello" : "world" }""")

      }
   }
   kafka {
      producer(10.milliseconds, "customer-events") {
         var counter = 0
         jsonMessage {
            // Ocacsionally emit a bad item (platinum doesn't exist)
            // val tier = listOf("platinum").random()
            val tier = listOf("gold", "silver" /*, "platinum", null */).random()
            val message = mapOf("eventId" to counter++, "name" to "Jimmy", "categoryId" to tier)
            println("Sending message: $message")

            message
         }
      }
   }
}


