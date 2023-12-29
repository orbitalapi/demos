package com.lunarbank.demo

import com.lunarbank.TypeNames.com.lunarbank.demo.LastTradeEvent
import lang.taxi.annotations.DataType

@DataType(
  value = LastTradeEvent,
  imported = true
)
open class LastTradeEvent(
  val orderId: OrderId,
  val instrumentId: Isin,
  val lastTradedPrice: LastTradedPrice,
  val venue: Venue
)
