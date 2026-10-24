package com.petflix.v2.idlookup

import lang.taxi.generators.kotlin.TypeAliasRegister
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

/**
 * Publishing to Orbital is handled by schema-publisher-spring-boot-starter: on startup it generates
 * Taxi for the @RestControllers in this package and sends it to Orbital. Where Orbital is, and what
 * the published project is called, is configured under orbital.schema.publisher in application.yml.
 */
@SpringBootApplication
class IdLookupServiceApp {
   companion object {
      @JvmStatic
      fun main(args: Array<String>) {
         // The generator only resolves Kotlin type aliases (FilmId, NetflixFilmId, ...) in
         // packages it has been told about. Without this, the published Taxi would say Int / String.
         TypeAliasRegister.registerPackage("films")
         TypeAliasRegister.registerPackage("films.reviews")
         TypeAliasRegister.registerPackage("demo.netflix")
         SpringApplication.run(IdLookupServiceApp::class.java, *args)
      }
   }
}
