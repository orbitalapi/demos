package com.orbitalhq.demos.trading.kafka

import com.orbitalhq.demos.trading.service.OrderAndLastTradedPricePublisher
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class PublisherService(private val kafkaTemplate: KafkaTemplate<String, ByteArray>,
   private val orderAndLastTradedPricePublisher: OrderAndLastTradedPricePublisher
) {

   @PostMapping("/kafka/{topic}")
   fun send(@PathVariable("topic") topic: String, @RequestBody message: String) {
      kafkaTemplate.send(topic, message.encodeToByteArray())
   }

   @GetMapping("/start")
   fun start() {
      orderAndLastTradedPricePublisher.start()
   }

   @GetMapping("/stop")
   fun stop() {
      orderAndLastTradedPricePublisher.stop()
   }
}
