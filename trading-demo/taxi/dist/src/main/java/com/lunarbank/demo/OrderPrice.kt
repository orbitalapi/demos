package com.lunarbank.demo

import com.lunarbank.TypeNames.com.lunarbank.demo.OrderPrice
import lang.taxi.annotations.DataType

@DataType(
  value = OrderPrice,
  imported = true
)
typealias OrderPrice = Price
