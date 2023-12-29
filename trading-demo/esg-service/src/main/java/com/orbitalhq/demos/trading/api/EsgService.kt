package com.orbitalhq.demos.trading.api

import com.lunarbank.demo.EsgRating
import com.lunarbank.demo.Isin
import com.orbitalhq.demos.trading.service.EsgRatingGenerator
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class EsgService(private val esgScoreProvider: EsgRatingGenerator) {
   @GetMapping("/esgratings/{isin}")
   fun getEsgRating(@PathVariable("isin") isin: Isin): EsgRating {
      return esgScoreProvider.esgRatingFor(isin)
   }
}
