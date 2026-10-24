package com.petflix.v2.reviews

import lang.taxi.generators.kotlin.TypeAliasRegister
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

/**
 * Publishing to Orbital is handled by schema-publisher-spring-boot-starter: on startup it generates
 * Taxi for the @RestControllers in this package and sends it to Orbital. Where Orbital is, and what
 * the published project is called, is configured under orbital.schema.publisher in application.yml.
 */
@SpringBootApplication
class SquashedTomatoesReviewsApp {
   companion object {
      @JvmStatic
      fun main(args: Array<String>) {
         // The generator only resolves Kotlin type aliases (SquashedTomatoesFilmId, ReviewText, ...)
         // in packages it has been told about. Without this, the published Taxi would say String.
         TypeAliasRegister.registerPackage("films")
         TypeAliasRegister.registerPackage("films.reviews")
         SpringApplication.run(SquashedTomatoesReviewsApp::class.java, *args)
      }
   }
}
