package com.lunarbank.demo

import com.lunarbank.TypeNames.com.lunarbank.demo.TradeExecutionTimestamp
import java.time.Instant
import lang.taxi.annotations.DataType

@DataType(
  value = TradeExecutionTimestamp,
  imported = true
)
typealias TradeExecutionTimestamp = Instant
