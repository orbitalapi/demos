package com.orbitalhq.demos.trading.service

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.lunarbank.demo.LastTradeEvent
import com.lunarbank.demo.Order
import com.lunarbank.demo.OrderStatus
import com.lunarbank.demo.OrderType
import com.lunarbank.demo.TimeInForce
import com.orbitalhq.demos.trading.config.KafkaPublicationConfig
import mu.KotlinLogging
import org.springframework.beans.factory.InitializingBean
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import reactor.core.Disposable
import reactor.core.publisher.Flux
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Duration
import java.time.temporal.ChronoUnit
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.LinkedBlockingQueue
import kotlin.random.Random

private val logger = KotlinLogging.logger {  }
@Component
class OrderAndLastTradedPricePublisher(
   referenceDataRepo: ReferenceDataRepo,
   private  val kafkaPublicationConfig: KafkaPublicationConfig,
   val kafkaTemplate: KafkaTemplate<String, ByteArray>
): InitializingBean {
   private val disposableMap = ConcurrentHashMap<String, Disposable>()
   private val instrumentIsins = referenceDataRepo.allIsins()
   private val traders = referenceDataRepo.allTraders()
   private val orderQueue = LinkedBlockingQueue<Order>()
   private val objectMapper = jacksonObjectMapper()
   private val venues = listOf("EuroTLX", "Swiss Exchange", "NYSE Bonds")

   fun start() {
      logger.info { "Starting order and last traded event publishers" }
      disposableMap.getOrPut(ordersTopic) {
         createOrderPublishing()
      }
      disposableMap.getOrPut(lastTradedEventsTopic) {
         createLastTradedEventPublishing()
      }
   }

   fun stop() {
      logger.info { "stopping order and last trade event publishers" }
      disposableMap.remove(ordersTopic)?.dispose()
      disposableMap.remove(lastTradedEventsTopic)?.dispose()
   }

   private fun createOrderPublishing(): Disposable {
     return Flux.interval(Duration.of(kafkaPublicationConfig.orderPublishPeriod, ChronoUnit.SECONDS))
         .subscribe {
            val instrumentIndex = Random.nextInt(0, instrumentIsins.size)
            val traderIndex = Random.nextInt(0, traders.size)
            val orderSize = Random.nextDouble(1.0, 10.0)
            val newOrder = Order(
               id = UUID.randomUUID().toString(),
               instrumentId = instrumentIsins[instrumentIndex],
               qtyRequested = orderSize.toBigDecimal().setScale(2, RoundingMode.HALF_UP),
               qtyHit = BigDecimal.ZERO,
               orderType = OrderType.Market,
               orderStatus = OrderStatus.New,
               tif = TimeInForce.GTC,
               traderId = traders[traderIndex],
               counterpartyId = UUID.randomUUID().toString()
            )
            val json = objectMapper.writeValueAsBytes(newOrder)
            kafkaTemplate.send(ordersTopic, json)
            logger.info { "published new order ${newOrder.id}" }
            orderQueue.add(newOrder)
         }
   }

   private fun createLastTradedEventPublishing(): Disposable {
      return Flux.interval(Duration.of(kafkaPublicationConfig.lastTradePublishPeriod, ChronoUnit.SECONDS))
         .subscribe {
            orderQueue.poll()?.let { order ->
               val venueIndex =  Random.nextInt(0, venues.size)
               val lastTradedEvent = LastTradeEvent(
                  orderId = order.id,
                  instrumentId =  order.instrumentId,
                  lastTradedPrice =  Random.nextDouble(100.0, 105.0).toBigDecimal().setScale(2, RoundingMode.HALF_UP),
                  venue = venues[venueIndex]
               )
               val json = objectMapper.writeValueAsBytes(lastTradedEvent)
               kafkaTemplate.send(lastTradedEventsTopic, json)
               logger.info { "published trader for order id ${lastTradedEvent.orderId}" }
            }
         }
   }

   companion object {
      private const val ordersTopic = "orders"
      private const val lastTradedEventsTopic = "trades"
   }

   override fun afterPropertiesSet() {
     this.start()
   }
}
