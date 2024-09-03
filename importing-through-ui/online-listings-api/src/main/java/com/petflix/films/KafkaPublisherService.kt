package com.petflix.films

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import kotlin.random.Random

@Component
class KafkaPublisherService(private val kafkaTemplate: KafkaTemplate<String, ByteArray>, private val objectMapper: ObjectMapper) {
    @Volatile
    private var  autoPublishingEnabled: Boolean = true
    private val protoLoader = ProtoLoader()

    fun getProtoSpec(): String {
        return protoLoader.protoSpec
    }
    @Scheduled(fixedRate = 1000)
    fun pushRandomAnnouncement() {
        if (!autoPublishingEnabled) {
            return
        }
        val filmId = Random.nextInt(2, 100)
        publishNewReleaseAnnouncement(filmId)
    }

    fun startPublish() {
        autoPublishingEnabled = true
    }

    fun stopPublish() {
        autoPublishingEnabled = false
    }
    fun publish(content: String, topic: String): KafkaPublisher.KafkaRecordMetadata {
        val sendResult = kafkaTemplate.send(topic, content.toByteArray()).get()
        return KafkaPublisher.KafkaRecordMetadata(
            sendResult.recordMetadata.topic(),
            sendResult.recordMetadata.offset(),
            sendResult.recordMetadata.partition(),
            sendResult.recordMetadata.timestamp()
        )
    }


    fun publishNewReleaseAnnouncement( filmId: Int): KafkaPublisher.KafkaRecordMetadata {
        val topicName = "releases"
        val announcement = mapOf(
            "filmId" to filmId,
            "announcement" to "Today, Netflix announced the reboot of yet another classic franchise"
        )

        KafkaPublisher.logger.info(
            "Sending message to Kafka topic '$topicName': \n" +
                    objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(announcement)
        )
        val protoMessage = protoLoader.protobufSchema.protoAdapter("NewFilmReleaseAnnouncement", false)
            .encode(announcement)
        val sendResult = kafkaTemplate.send(
            topicName,
            protoMessage
        ).get()
        return KafkaPublisher.KafkaRecordMetadata(
            sendResult.recordMetadata.topic(),
            sendResult.recordMetadata.offset(),
            sendResult.recordMetadata.partition(),
            sendResult.recordMetadata.timestamp()
        )
    }
}