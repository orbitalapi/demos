package com.lunarbank.demo

import com.lunarbank.TypeNames.com.lunarbank.demo.OrderStatus
import lang.taxi.annotations.DataType

@DataType(
  value = OrderStatus,
  imported = true
)
enum class OrderStatus {
  New,

  PartiallyFilled,

  Filled,

  Canceled,

  Rejected
}
