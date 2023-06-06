package com.surely.quotes.risks

import com.surely.quotes.taxonomy.OccupationCode
import com.surely.quotes.taxonomy.OccupationRatingFactor
import lang.taxi.annotations.ParameterType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class OccupationalRiskFactorService {

   @PostMapping("/enrichment/risks/occupation")
   fun calculateOccupationalRiskFactor(@RequestBody request: OccupationRiskRequest): OccupationalRiskFactorResult {
      return OccupationalRiskFactorResult((OccupationRatingFactor.LowRisk))
   }
}

@ParameterType
data class OccupationRiskRequest(
   val occupation: OccupationCode
)

data class OccupationalRiskFactorResult(val risk: OccupationRatingFactor)
