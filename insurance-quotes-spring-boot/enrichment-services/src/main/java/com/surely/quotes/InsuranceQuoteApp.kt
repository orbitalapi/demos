package com.surely.quotes

import com.orbital.client.OrbitalTransport
import com.orbital.client.transport.okhttp.http
import io.vyne.PackageMetadata
import io.vyne.schema.publisher.SchemaPublisherService
import io.vyne.schema.publisher.rsocket.RSocketSchemaPublisherTransport
import io.vyne.schema.rsocket.TcpAddress
import lang.taxi.generators.java.spring.SpringTaxiGenerator
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component


@SpringBootApplication
class InsuranceQuoteApp {
   companion object {
      @JvmStatic
      fun main(args: Array<String>) {
         SpringApplication.run(InsuranceQuoteApp::class.java, *args)
      }
   }
}

@Configuration
class OrbitalClientConfig {
   @Bean
   fun orbitalTransport(): OrbitalTransport {
      return http("http://localhost:9022")
   }
}

@Component
class RegisterSchemaOnStartup(
   @Value("\${server.port}")
   private val serverPort: String,
   @Value("\${spring.application.name}")
   private val appName: String
) {
   init {
      val publisher = SchemaPublisherService(
         appName,
         RSocketSchemaPublisherTransport(
            TcpAddress("localhost", 7655)
         )
      )
      val sources = SpringTaxiGenerator.forBaseUrl("http://localhost:${serverPort}")
         .forPackage(InsuranceQuoteApp::class.java)
         .generate()
      publisher.publish(
         PackageMetadata.from("com.surely", appName),
         sources
      ).subscribe()
   }
}
