package com.petflix.v2.reviews

import com.orbitalhq.PackageMetadata
import com.orbitalhq.schema.publisher.SchemaPublisherService
import com.orbitalhq.schema.publisher.rsocket.RSocketSchemaPublisherTransport
import com.orbitalhq.schema.rsocket.TcpAddress
import lang.taxi.generators.java.TaxiGenerator
import lang.taxi.generators.java.spring.SpringMvcExtension
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component

@SpringBootApplication
open class SquashedTomatoesReviewsApp {
   companion object {
      @JvmStatic
      fun main(args: Array<String>) {
         SpringApplication.run(SquashedTomatoesReviewsApp::class.java, *args)
      }
   }
}


@Configuration
open class OrbitalConfig {

   @Value("\${spring.application.name}")
   private val appName: String? = null

   @Bean
   open fun schemaPublisher(): SchemaPublisherService =
      SchemaPublisherService(
         appName!!,
         RSocketSchemaPublisherTransport(
            TcpAddress("localhost", 7655)
         ),
      )
}

@Component
class RegisterSchemaTask(
   publisher: SchemaPublisherService,
   @Value("\${server.port}") private val serverPort: String
) {
   init {
      publisher.publish(
         PackageMetadata.from("io.petflix.demos", "films-reviews"),
         TaxiGenerator()
            .forPackage(SquashedTomatoesReviewsApp::class.java)
            .addExtension(SpringMvcExtension("http://localhost:${serverPort}"))
            .generate(),
      ).subscribe()
   }
}
