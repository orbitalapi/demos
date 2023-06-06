package com.surely.quotes.risks

import com.surely.quotes.taxonomy.NightParkingPostCodeRiskFactor
import com.surely.quotes.taxonomy.OvernightParkingLocation
import com.surely.quotes.taxonomy.PostCode
import lang.taxi.annotations.DataType
import lang.taxi.annotations.ParameterType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class NightParkingRiskFactorService {

   @PostMapping("/risks/night-parking")
   fun getOvernightRiskParking(@RequestBody request: OvernightParkingRiskRequest): OvernightParkingRiskCalculation {
      // Locations with 2-letter postcodes are low risk
      if (request.postcode.length <= 2) {
         return OvernightParkingRiskCalculation(NightParkingPostCodeRiskFactor.LowRisk)
      }
      val risk = when (request.parkingLocation) {
         OvernightParkingLocation.Street -> NightParkingPostCodeRiskFactor.HighRisk
         OvernightParkingLocation.Garage -> NightParkingPostCodeRiskFactor.LowRisk
         OvernightParkingLocation.OffStreet -> NightParkingPostCodeRiskFactor.MediumRisk
      }
      return OvernightParkingRiskCalculation(risk)
   }
}

@ParameterType
data class OvernightParkingRiskRequest(
   val parkingLocation: OvernightParkingLocation,
   val postcode: PostCode
)

@DataType
data class OvernightParkingRiskCalculation(
   val risk: NightParkingPostCodeRiskFactor
)
