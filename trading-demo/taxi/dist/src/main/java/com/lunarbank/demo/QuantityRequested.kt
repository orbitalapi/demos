package com.lunarbank.demo

import com.lunarbank.TypeNames.com.lunarbank.demo.QuantityRequested
import lang.taxi.annotations.DataType

@DataType(
  value = QuantityRequested,
  imported = true
)
typealias QuantityRequested = Quantity
