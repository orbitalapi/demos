package com.orbitalhq.demos.xml.films

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication


@SpringBootApplication
class FilmsOnXmlApp {
   companion object {
      @JvmStatic
      fun main(args: Array<String>) {
         SpringApplication.run(FilmsOnXmlApp::class.java, *args)
      }
   }
}

data class Film(
   @JacksonXmlProperty(isAttribute = true)
   val id: String,
   val title: String,
   val yearReleased: Int
)

@JacksonXmlRootElement(localName = "actor")
data class Actor(
   val id: String,
   val name: String
)

@JacksonXmlRootElement(localName = "award")
data class Award(
   val title: String,
   val yearWon: Int
)

