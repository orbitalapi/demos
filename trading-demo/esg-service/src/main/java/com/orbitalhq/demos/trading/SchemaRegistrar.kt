package com.orbitalhq.demos.trading

import com.orbitalhq.PackageMetadata
import com.orbitalhq.demos.trading.api.EsgService
import com.orbitalhq.schema.publisher.SchemaPublisherService
import com.orbitalhq.schema.publisher.rsocket.RSocketSchemaPublisherTransport
import com.orbitalhq.schema.rsocket.TcpAddress
import lang.taxi.generators.java.spring.SpringTaxiGenerator
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class SchemaRegistrar(@Value("\${server.port}")
                      private val serverPort: String): InitializingBean {
   override fun afterPropertiesSet() {
      val appName = "trading-demo-service"
      val publisher = SchemaPublisherService(
         appName,
         RSocketSchemaPublisherTransport(
            TcpAddress("localhost", 7655)
         )
      )
      publisher.publish(
         PackageMetadata.from("com.lunarbank.demo", appName),
         SpringTaxiGenerator.forBaseUrl("http://localhost:${serverPort}")
            .forPackage(EsgService::class.java)
            .generate()
      ).subscribe()
   }
}
