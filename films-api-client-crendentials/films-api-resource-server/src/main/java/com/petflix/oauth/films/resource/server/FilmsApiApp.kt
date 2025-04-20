package com.petflix.oauth.films.resource.server

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
open class FilmsApiApp {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            SpringApplication.run(FilmsApiApp::class.java, *args)
        }
    }
}