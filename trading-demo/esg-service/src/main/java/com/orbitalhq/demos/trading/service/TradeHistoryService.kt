package com.orbitalhq.demos.trading.service

import com.lunarbank.demo.Isin
import com.lunarbank.demo.OrderId
import com.lunarbank.demo.Quantity
import com.lunarbank.demo.TradeExecutionTimestamp
import com.lunarbank.demo.TraderId
import com.lunarbank.demo.TraderRecord
import lang.taxi.annotations.ResponseConstraint
import lang.taxi.annotations.ResponseContract
import org.http4k.lens.QueryLens
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.UUID
import kotlin.random.Random

@RestController
class TradeHistoryService(
   private val referenceDataRepo: ReferenceDataRepo,
) {

   private val trades = mutableListOf<Trade>()

   init {
       buildHistoricTrades()
   }

   @GetMapping("/trades/history")
   fun listHistoricTrades(
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
      @RequestParam("startDate") startDate: LocalDate,
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
      @RequestParam("endDate") endDate: LocalDate
   ): List<Trade> {
      // Very non-performant, but this is demo code.
      return trades.filter { trade -> trade.executionDate.isAfter(startDate.atStartOfDay()
         .toInstant(ZoneOffset.UTC)) && trade.executionDate.isBefore(endDate.atStartOfDay().toInstant(ZoneOffset.UTC)) }
   }

   private fun buildHistoricTrades() {
      // Repeat for every day of the last year
      repeat(365) { daysOffset ->
         // Create a random number of trades per day
         repeat(Random.nextInt(5,30)) {
            val executionSeconds = Random.nextInt(0, 86000)
            val executionTimestamp = LocalDate.now().minusDays(daysOffset.toLong())
               .atStartOfDay(ZoneId.systemDefault())
               .plusSeconds(executionSeconds.toLong())
               .toInstant()
            trades.add(Trade(
               UUID.randomUUID().toString(),
               executionTimestamp,
               referenceDataRepo.allIsins().random(),
               referenceDataRepo.allTraders().random(),
               Random.nextInt(100_000, 200_000_000).toBigDecimal()
            ))
         }

      }
   }
}


data class Trade(
   val id: OrderId,
   val executionDate: TradeExecutionTimestamp,
   val isin: Isin,
   val traderId: TraderId,
   val quantity: Quantity
)
