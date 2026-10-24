package com.petflix.v2.listings

import lang.taxi.generators.kotlin.TypeAliasRegister
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.info.GitProperties
import org.springframework.scheduling.annotation.EnableScheduling

/**
 * Publishing to Orbital is handled by schema-publisher-spring-boot-starter: on startup it generates
 * Taxi for the @RestControllers in this package and sends it to Orbital. Where Orbital is, and what
 * the published project is called, is configured under orbital.schema.publisher in application.yml.
 */
@SpringBootApplication
@EnableScheduling
class FilmListingsApp {

   companion object {
      val logger = LoggerFactory.getLogger(FilmListingsApp::class.java)

      @JvmStatic
      fun main(args: Array<String>) {
         // The generator only resolves Kotlin type aliases (FilmId, StreamingProviderName, ...) in
         // packages it has been told about. Without this, the published Taxi would say Int / String.
         TypeAliasRegister.registerPackage("films")
         TypeAliasRegister.registerPackage("films.reviews")
         TypeAliasRegister.registerPackage("demo.netflix")
         SpringApplication.run(FilmListingsApp::class.java, *args)
      }
   }

   @Autowired
   fun logInfo(@Autowired(required = false) buildInfo: GitProperties? = null) {
      val commit = buildInfo?.shortCommitId ?: "Unknown"
      logger.info("Films API built on commit $commit")
   }
}
