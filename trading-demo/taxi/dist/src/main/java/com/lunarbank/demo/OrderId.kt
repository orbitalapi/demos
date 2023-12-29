package com.lunarbank.demo

import com.lunarbank.TypeNames.com.lunarbank.demo.OrderId
import kotlin.String
import lang.taxi.annotations.DataType

@DataType(
  value = OrderId,
  imported = true
)
typealias OrderId = String
