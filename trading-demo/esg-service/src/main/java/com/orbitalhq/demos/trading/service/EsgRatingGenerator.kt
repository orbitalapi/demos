package com.orbitalhq.demos.trading.service


import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.lunarbank.demo.EsgRating
import com.lunarbank.demo.Isin
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Service
import java.math.RoundingMode
import java.nio.file.Paths
import kotlin.random.Random

@Service
class EsgRatingGenerator {
   private val esgRatingsMap: Map<Isin, EsgRating>
   init {
      val typeRef = object: TypeReference<List<EsgRating>>(){}

      val resource = ClassPathResource("esgratings.json")
      val resourceBytes = resource.inputStream.use {
         it.readAllBytes()
      }

      esgRatingsMap = jacksonObjectMapper()
         .readValue(resourceBytes, typeRef)
         .associateBy { it.isin }
   }
   fun esgRatingFor(isin: Isin): EsgRating {
      return esgRatingsMap[isin] ?: EsgRating(
         isin = isin,
         environmentalPillarScore = Random.nextDouble(1.0, 10.0).toBigDecimal().setScale(2, RoundingMode.HALF_EVEN),
         socialPillarScore = Random.nextDouble(1.0, 10.0).toBigDecimal().setScale(2, RoundingMode.HALF_EVEN),
         governancePillarScore = Random.nextDouble(1.0, 10.0).toBigDecimal().setScale(2, RoundingMode.HALF_EVEN),
         carbonIntensityWeightedAverage = Random.nextDouble(10.0, 100.0).toBigDecimal().setScale(2, RoundingMode.HALF_EVEN))
   }
}
