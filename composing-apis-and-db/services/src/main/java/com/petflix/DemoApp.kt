package com.petflix

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

/**
 * A single Spring Boot app hosting the three demo REST APIs (reviews, streaming providers
 * and id lookups), to keep the demo small. In practice these would be separate services.
 *
 * Orbital learns about these APIs from the OpenAPI spec (api-docs.yaml, in the workspace under
 * ../orbital_data), which is annotated with x-taxi-type. The app itself doesn't talk to Orbital.
 * For publishing a schema straight from a Spring Boot app, see the publishing-from-apps demo.
 */
@SpringBootApplication
class DemoApp {
   companion object {
      @JvmStatic
      fun main(args: Array<String>) {
         SpringApplication.run(DemoApp::class.java, *args)
      }
   }
}
