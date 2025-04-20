package com.petflix.oauth.films.data

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
open class FilmsDataApi {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            SpringApplication.run(FilmsDataApi::class.java, *args)
        }
    }
}