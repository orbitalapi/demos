package com.lunarbank.demo

import com.lunarbank.TypeNames.com.lunarbank.demo.TraderRecord
import lang.taxi.annotations.DataType

@DataType(
  value = TraderRecord,
  imported = true
)
open class TraderRecord(
  val id: TraderId,
  val name: TraderName,
  val lastName: TraderLastName,
  val deskId: DeskId
)
