package com.lunarbank.demo

import com.lunarbank.TypeNames.com.lunarbank.demo.EnvironmentalPillarScore
import java.math.BigDecimal
import lang.taxi.annotations.DataType

@DataType(
  value = EnvironmentalPillarScore,
  imported = true
)
typealias EnvironmentalPillarScore = BigDecimal
