package com.surely.quotes.risks

import com.surely.quotes.taxonomy.CarLicensePlate
import com.surely.quotes.taxonomy.Manufacturer
import com.surely.quotes.taxonomy.ModelName
import com.surely.quotes.taxonomy.VehicleRiskFactor
import lang.taxi.annotations.ParameterType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class VehicleInfoService {

   private val responses = listOf(
      MakeAndModelResponse(Manufacturer.Volvo, "XC60"),
      MakeAndModelResponse(Manufacturer.BMW, "M3"),
      MakeAndModelResponse(Manufacturer.Audi, "Q3"),
   )

   @PostMapping("/vehicles/info")
   fun lookupMakeAndModel(@RequestBody request: MakeAndModelRequest): MakeAndModelResponse {
      return responses.random()
   }
}


@RestController
class VehicleRiskFactorService {
   @PostMapping("/enrichment/risks/vehicles")
   fun calculateVehcileRiskFactor(@RequestBody request: VehicleRiskFactorRequest): VehicleRiskFactorResponse {
      return VehicleRiskFactorResponse(VehicleRiskFactor.values().random())
   }
}


// Note that even within this domain, we can't agree on names - carMaker vs manufacturer, modelName vs model
@ParameterType
data class VehicleRiskFactorRequest(val carMaker: Manufacturer, val model: ModelName)
data class VehicleRiskFactorResponse(val riskFactor: VehicleRiskFactor)
data class MakeAndModelResponse(
   val manufacturer: Manufacturer, val modelName: ModelName
)

@ParameterType
data class MakeAndModelRequest(
   val plateNumber: CarLicensePlate
)
