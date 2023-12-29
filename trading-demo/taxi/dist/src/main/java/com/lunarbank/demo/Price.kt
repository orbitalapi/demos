package com.lunarbank.demo

import com.lunarbank.TypeNames.com.lunarbank.demo.Price
import java.math.BigDecimal
import lang.taxi.annotations.DataType

@DataType(
  value = Price,
  imported = true
)
typealias Price = BigDecimal
