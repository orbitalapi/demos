import io.ktor.http.*
import io.ktor.util.*

stack {
   http {
      post("/report") { call ->

         println("Request recevied - Body: \n ${call.receiveText()}")
         call.respondText("""{ "message" : "Great, thanks" }""")
      }
      post("/report-single") { call ->
         println(
            """Request recieved - Body:
 Body: ${call.receiveText()}
 Headers: ${call.request.headers.toMap()}
 """
         )
         if (Random.nextInt(0,3) == 2) {
            call.respondText("""{ "message" : "Nope" }""", status = HttpStatusCode.Unauthorized)
         } else {
            call.respondText("""{ "message" : "Great, thanks" }""")
         }

      }
   }

}
