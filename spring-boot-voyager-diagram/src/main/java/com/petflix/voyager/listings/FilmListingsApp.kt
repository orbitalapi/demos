package com.petflix.voyager.listings

import io.vyne.PackageMetadata
import io.vyne.schema.publisher.SchemaPublisherService
import io.vyne.schema.publisher.rsocket.RSocketSchemaPublisherTransport
import io.vyne.schema.rsocket.TcpAddress
import lang.taxi.generators.java.spring.SpringTaxiGenerator
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.info.GitProperties
import org.springframework.stereotype.Component

@SpringBootApplication
class FilmListingsApp {

   companion object {
      val logger = LoggerFactory.getLogger(this::class.java)

      @JvmStatic
      fun main(args: Array<String>) {
         SpringApplication.run(FilmListingsApp::class.java, *args)
      }
   }

   @Autowired
   fun logInfo(@Autowired(required = false) buildInfo: GitProperties? = null) {
      val commit = buildInfo?.shortCommitId ?: "Unknown"
      logger.info("Films API built on commit $commit")
   }
}


/**
 * Create a task, which runs on startup, generating & publishing our schema
 */
@Component
class RegisterSchemaTask(
   @Value("\${spring.application.name}") appName: String,
   @Value("\${server.port}") private val serverPort: String
) {
   init {
      /**
       * Declare a publisher, which will connect to the local running Orbital,
       * and publish the generated schema
       */
      val publisher = SchemaPublisherService(
         appName,
         RSocketSchemaPublisherTransport(
            // Orbital accepts schema submissions over
            // RSocket using port 7655
            TcpAddress("localhost", 7655)
         )
      )
      publisher.publish(
         // Package metadata identifies this project.
         // Kinda like maven poms, or npm package.json
         // This can be whatever you want, but make it unique to this service
         PackageMetadata.from(
            organisation = "io.petflix.demos",
            name = "films-listings"
         ),
         // The generator creates Taxi schemas.
         // The base url should be the url of this application.
         // All published URLs are relative to this url
         SpringTaxiGenerator.forBaseUrl("http://localhost:${serverPort}")
            .forPackage(FilmListingsApp::class.java)
            .generate()
      ).subscribe()
   }
}
