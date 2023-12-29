package com.lunarbank.demo

import com.lunarbank.TypeNames.com.lunarbank.demo.TraderId
import kotlin.Int
import lang.taxi.annotations.DataType

@DataType(
  value = TraderId,
  imported = true
)
typealias TraderId = Int
