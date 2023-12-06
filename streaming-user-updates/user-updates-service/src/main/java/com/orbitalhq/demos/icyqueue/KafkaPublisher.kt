package com.orbitalhq.demos.icyqueue

import org.springframework.kafka.core.KafkaTemplate
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class KafkaPublisher(val kafkaTemplate: KafkaTemplate<String, ByteArray>) {

   @PostMapping("/kafka/{topic}")
   fun write(@PathVariable("topic") topic: String, @RequestBody message: String) {
      kafkaTemplate.send(topic, message.encodeToByteArray())
   }
}
