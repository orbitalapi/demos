package com.lunarbank.demo

import com.lunarbank.TypeNames.com.lunarbank.demo.LastTradedPrice
import lang.taxi.annotations.DataType

@DataType(
  value = LastTradedPrice,
  imported = true
)
typealias LastTradedPrice = Price
