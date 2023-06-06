package com.surely.quotes.vehicles

import com.surely.TypeNames.com.surely.quotes.vehicles.VehicleRiskFactor
import lang.taxi.annotations.DataType

@DataType(
  value = VehicleRiskFactor,
  imported = true
)
enum class VehicleRiskFactor {
  HighRisk,

  MediumRisk,

  LowRisk
}
