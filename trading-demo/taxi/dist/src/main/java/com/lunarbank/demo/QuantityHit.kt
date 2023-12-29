package com.lunarbank.demo

import com.lunarbank.TypeNames.com.lunarbank.demo.QuantityHit
import lang.taxi.annotations.DataType

@DataType(
  value = QuantityHit,
  imported = true
)
typealias QuantityHit = Quantity
