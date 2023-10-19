package com.orbitalhq.demos.icyqueue

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.google.common.cache.CacheBuilder
import com.google.common.collect.EvictingQueue
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import java.time.Duration
import kotlin.random.Random

@Component
class UserUpdatePublisher(private val userGenerator: UserGenerator,
                          val kafkaTemplate: KafkaTemplate<String, ByteArray>) {
   private val users = userGenerator.users
      .subList(0,10)
   private val objectMapper = jacksonObjectMapper()
   companion object {
      val logger = LoggerFactory.getLogger(UserUpdatePublisher::class.java)
   }

   private val recentMessages = EvictingQueue.create<UserStatusUpdateMessage>(300)
   private val analytics = CacheBuilder.newBuilder()
      .maximumSize(30) // A bit bigger than the recent messages
      .build<Int,MessageAnalytics>()

   fun getPostById(messageId: Int): UserStatusUpdateMessage? {
      return recentMessages.firstOrNull { it.messageId == messageId }
   }
   init {
      Flux.interval(Duration.ofMillis(750))
         .map {
            StatusGenerator.generateRandomStatusUpdate(users)
         }.subscribe { userStatusUpdateMessage ->
            logger.info("New status: $userStatusUpdateMessage")
            recentMessages.add(userStatusUpdateMessage)
            val json = objectMapper.writeValueAsBytes(userStatusUpdateMessage)
            kafkaTemplate.send("UserUpdates", json)
         }


      val f = Flux.interval(Duration.ofMillis(500))
         .mapNotNull {
            recentMessages.randomOrNull()?.let { message ->
               val lastAnalytics = analytics.get(message.messageId) {
                  MessageAnalytics(message.messageId, 0)
               }
               val increasedViewCount = Random.nextInt(50, 5000)
               val updatedAnalytics = lastAnalytics.increaseViewCount(increasedViewCount)
               analytics.put(message.messageId, updatedAnalytics)
               updatedAnalytics
            }
         }
         .subscribe { messageAnalytics ->
            logger.info("Emitting updated analytics: $messageAnalytics")
            val json = objectMapper.writeValueAsBytes(messageAnalytics)
            kafkaTemplate.send("MessageAnalytics", json)

         }
   }
}
