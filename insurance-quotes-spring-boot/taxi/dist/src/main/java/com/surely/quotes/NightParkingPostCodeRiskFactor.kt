package com.surely.quotes

import com.surely.TypeNames.com.surely.quotes.NightParkingPostCodeRiskFactor
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
