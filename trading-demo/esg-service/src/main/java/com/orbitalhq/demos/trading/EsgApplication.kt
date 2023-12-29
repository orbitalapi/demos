package com.orbitalhq.demos.trading

import com.orbitalhq.demos.trading.config.KafkaPublicationConfig
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties

@SpringBootApplication
@EnableConfigurationProperties(KafkaPublicationConfig::class)
class EsgApplication {
   companion object {
      @JvmStatic
      fun main(args: Array<String>) {
         SpringApplication.run(EsgApplication::class.java, *args)
      }
   }
}
