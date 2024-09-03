package com.petflix.films

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.web.bind.annotation.*
import kotlin.random.Random

@RestController
class KafkaPublisher(private val kafkaPublisherService: KafkaPublisherService, val objectMapper: ObjectMapper) {

    companion object {
        val logger = LoggerFactory.getLogger(this::class.java)
    }

    data class KafkaRecordMetadata(
        val topic: String,
        val offset: Long,
        val partition: Int,
        val timestamp: Long
    )

    @GetMapping("/proto")
    fun getProtoSpec(): String {
        return kafkaPublisherService.getProtoSpec()
    }

    @PostMapping("/kafka/{topic}", consumes = ["*/*"])
    fun publish(@RequestBody content: String, @PathVariable("topic") topic: String): KafkaRecordMetadata {
        return kafkaPublisherService.publish(content, topic)
    }

    @GetMapping("/announcements/start")
    fun startAutoPublishing() {
        kafkaPublisherService.startPublish()
        kafkaPublisherService.pushRandomAnnouncement()
    }

    @GetMapping("/announcements/stop")
    fun stopAutoPublishing() {
        kafkaPublisherService.stopPublish()
    }

    @PostMapping("/kafka/newReleases/{filmId}")
    fun publishNewReleaseAnnouncement(@PathVariable("filmId") filmId: Int): KafkaRecordMetadata {
        return kafkaPublisherService.publishNewReleaseAnnouncement(filmId)
    }
}
