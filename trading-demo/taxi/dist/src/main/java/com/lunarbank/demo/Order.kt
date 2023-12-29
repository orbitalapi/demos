package com.lunarbank.demo

import com.lunarbank.TypeNames.com.lunarbank.demo.Order
import lang.taxi.annotations.DataType

@DataType(
  value = Order,
  imported = true
)
open class Order(
  val id: OrderId,
  val instrumentId: Isin,
  val qtyRequested: QuantityRequested,
  val qtyHit: QuantityHit,
  val orderType: OrderType,
  val orderStatus: OrderStatus,
  val tif: TimeInForce,
  val traderId: TraderId,
  val counterpartyId: CounterpartyId
)
