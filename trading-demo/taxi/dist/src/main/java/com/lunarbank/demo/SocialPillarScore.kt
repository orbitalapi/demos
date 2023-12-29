package com.lunarbank.demo

import com.lunarbank.TypeNames.com.lunarbank.demo.SocialPillarScore
import java.math.BigDecimal
import lang.taxi.annotations.DataType

@DataType(
  value = SocialPillarScore,
  imported = true
)
typealias SocialPillarScore = BigDecimal
