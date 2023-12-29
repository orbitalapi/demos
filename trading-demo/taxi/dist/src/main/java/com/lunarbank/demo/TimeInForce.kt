package com.lunarbank.demo

import com.lunarbank.TypeNames.com.lunarbank.demo.TimeInForce
import lang.taxi.annotations.DataType

@DataType(
  value = TimeInForce,
  imported = true
)
enum class TimeInForce {
  Day,

  GTC,

  IOC,

  FOK
}
