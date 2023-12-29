package com.lunarbank.demo

import com.lunarbank.TypeNames.com.lunarbank.demo.OrderType
import lang.taxi.annotations.DataType

@DataType(
  value = OrderType,
  imported = true
)
enum class OrderType {
  Market,

  Limit,

  Stop,

  StopLimit
}
