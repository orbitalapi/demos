package com.surely.quotes

import com.surely.TypeNames.com.surely.quotes.ResidentialPostCodeRiskFactor
import lang.taxi.annotations.DataType

@DataType(
  value = ResidentialPostCodeRiskFactor,
  imported = true
)
enum class ResidentialPostCodeRiskFactor {
  HighRisk,

  MediumRisk,

  LowRisk
}
