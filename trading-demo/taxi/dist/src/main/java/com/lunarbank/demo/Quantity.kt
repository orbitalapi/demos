package com.lunarbank.demo

import com.lunarbank.TypeNames.com.lunarbank.demo.Quantity
import java.math.BigDecimal
import lang.taxi.annotations.DataType

@DataType(
  value = Quantity,
  imported = true
)
typealias Quantity = BigDecimal
