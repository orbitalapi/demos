package com.petflix.oauth.server.bootstrap

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
open class AuthorisationServerApp {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            SpringApplication.run(AuthorisationServerApp::class.java, *args)
        }
    }
}
