package com.surely.quotes.vehicles

import com.surely.TypeNames.com.surely.quotes.vehicles.NightParkingPostCodeRiskFactor
import lang.taxi.annotations.DataType

@DataType(
  value = NightParkingPostCodeRiskFactor,
  imported = true
)
enum class NightParkingPostCodeRiskFactor {
  HighRisk,

  MediumRisk,

  LowRisk
}
