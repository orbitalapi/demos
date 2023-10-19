package com.orbitalhq.demos.icyqueue

import com.orbitalhq.PackageMetadata
import com.orbitalhq.demos.icyqueue.api.UserService
import com.orbitalhq.schema.publisher.SchemaPublisherService
import com.orbitalhq.schema.publisher.rsocket.RSocketSchemaPublisherTransport
import com.orbitalhq.schema.rsocket.TcpAddress
import lang.taxi.generators.java.spring.SpringTaxiGenerator
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.stereotype.Component

@SpringBootApplication
class IcyCubeApp {
   companion object {
      @JvmStatic
      fun main(args: Array<String>) {
         SpringApplication.run(IcyCubeApp::class.java, *args)
      }
   }
}

@Component
class RegisterSchemaOnStartup(
   @Value("\${server.port}")
   private val serverPort: String,
) {
   init {
      val appName = "user-updates-service"
      val publisher = SchemaPublisherService(
         appName,
         RSocketSchemaPublisherTransport(

            TcpAddress("localhost", 7655)
         )
      )
      publisher.publish(
         PackageMetadata.from("com.icyqueue.demos", appName),
         SpringTaxiGenerator.forBaseUrl("http://localhost:${serverPort}")
            .forPackage(UserService::class.java)
            .forPackage(User::class.java)
            .generate()
      ).subscribe()
   }
}
