package com.surely.quotes

import com.surely.TypeNames.com.surely.quotes.RiskFactor
import lang.taxi.annotations.DataType

@DataType(
  value = RiskFactor,
  imported = true
)
enum class RiskFactor {
  HighRisk,

  MediumRisk,

  LowRisk
}
