package com.surely.quotes

import com.surely.TypeNames.com.surely.quotes.VehicleRiskFactor
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
