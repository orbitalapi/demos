stack {
   http {
      post("/report") { call ->

         println("Request recevied - Body: \n ${call.receiveText()}")
         call.respondText("""{ "message" : "Great, thanks" }""")
      }
   }

}
