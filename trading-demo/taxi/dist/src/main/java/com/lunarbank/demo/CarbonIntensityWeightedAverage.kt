package com.lunarbank.demo

import com.lunarbank.TypeNames.com.lunarbank.demo.CarbonIntensityWeightedAverage
import java.math.BigDecimal
import lang.taxi.annotations.DataType

@DataType(
  value = CarbonIntensityWeightedAverage,
  imported = true
)
typealias CarbonIntensityWeightedAverage = BigDecimal
