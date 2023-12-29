package com.lunarbank.demo

import com.lunarbank.TypeNames.com.lunarbank.demo.GovernancePillarScore
import java.math.BigDecimal
import lang.taxi.annotations.DataType

@DataType(
  value = GovernancePillarScore,
  imported = true
)
typealias GovernancePillarScore = BigDecimal
