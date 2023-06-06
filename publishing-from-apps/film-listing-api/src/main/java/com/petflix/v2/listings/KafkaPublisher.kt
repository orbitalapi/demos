package com.petflix.v2.listings

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.web.bind.annotation.*
import kotlin.random.Random

@RestController
class KafkaPublisher(val kafkaTemplate: KafkaTemplate<String, ByteArray>, val objectMapper: ObjectMapper) {

    private val protoLoader = ProtoLoader()
    private var autoPublishingEnabled = true

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
        return protoLoader.protoSpec
    }

    @PostMapping("/kafka/{topic}", consumes = ["*/*"])
    fun publish(@RequestBody content: String, @PathVariable("topic") topic: String): KafkaRecordMetadata {
        val sendResult = kafkaTemplate.send(topic, content.toByteArray()).get()
        return KafkaRecordMetadata(
            sendResult.recordMetadata.topic(),
            sendResult.recordMetadata.offset(),
            sendResult.recordMetadata.partition(),
            sendResult.recordMetadata.timestamp()
        )
    }

    @GetMapping("/announcements/start")
    fun startAutoPublishing() {
        autoPublishingEnabled = true
        pushRandomAnnouncement()
    }

    @GetMapping("/announcements/stop")
    fun stopAutoPublishing() {
        autoPublishingEnabled = false
    }

    @Scheduled(fixedRate = 1000)
    fun pushRandomAnnouncement() {
        if (!autoPublishingEnabled) {
            return
        }
        val filmId = Random.nextInt(2, 100)
        publishNewReleaseAnnouncement(filmId)
    }


    @PostMapping("/kafka/newReleases/{filmId}")
    fun publishNewReleaseAnnouncement(@PathVariable("filmId") filmId: Int): KafkaRecordMetadata {
        val topicName = "releases"
        val announcement = mapOf(
            "filmId" to filmId + 1000,
            "announcement" to "Today, Netflix announced the reboot of yet another classic franchise"
        )

        logger.info(
            "Sending message to Kafka topic '$topicName': \n" +
                    objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(announcement)
        )
        val protoMessage = protoLoader.protobufSchema.protoAdapter("NewFilmReleaseAnnouncement", false)
            .encode(announcement)
        val sendResult = kafkaTemplate.send(
            topicName,
            protoMessage
        ).get()
        return KafkaRecordMetadata(
            sendResult.recordMetadata.topic(),
            sendResult.recordMetadata.offset(),
            sendResult.recordMetadata.partition(),
            sendResult.recordMetadata.timestamp()
        )
    }
}
