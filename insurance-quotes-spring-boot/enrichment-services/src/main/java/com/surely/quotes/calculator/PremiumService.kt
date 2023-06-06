package com.surely.quotes.calculator

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.surely.quotes.*
import com.surely.quotes.taxonomy.*
import lang.taxi.annotations.DataType
import lang.taxi.annotations.ParameterType
import lang.taxi.utils.log
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal


@RestController
class PremiumService {

   @PostMapping("/premium")
   fun calculatePremium(@RequestBody request: QuoteWithRiskData): InsurancePremiumQuote {
      val baseCost = BigDecimal.valueOf(200.00)

      val requestJson = jacksonObjectMapper().writerWithDefaultPrettyPrinter()
         .writeValueAsString(request)
      log().info("Received request: \n$requestJson")

      val riskFactors = listOf(request.occupationRatingFactor.name, request.nightParkingRisk.name)
         .map { RiskFactor.valueOf(it) }

      // Add a premium for all the risk factors
      val annualCost = riskFactors.fold(baseCost) { acc, riskFactor ->
         when (riskFactor) {
            RiskFactor.LowRisk -> acc
            RiskFactor.MediumRisk -> acc.multiply(1.2.toBigDecimal())
            RiskFactor.HighRisk -> acc.multiply(1.4.toBigDecimal())
         }
      }

      return InsurancePremiumQuote(request.quoteId, annualCost)
   }
}

@DataType("InsurancePremiumQuote")
data class InsurancePremiumQuote(
   val quoteId: QuoteId,
   val annualCost: AnnualPremium
)

@ParameterType
data class QuoteWithRiskData(
   val quoteId: QuoteId,
   val occupation: OccupationCode,
   val occupationRatingFactor: OccupationRatingFactor,
   val nightParkingRisk: NightParkingPostCodeRiskFactor,
   val carRisk: VehicleRiskFactor
)
