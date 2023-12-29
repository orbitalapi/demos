package com.lunarbank.demo

import com.lunarbank.TypeNames.com.lunarbank.demo.DeskRecord
import lang.taxi.annotations.DataType

@DataType(
  value = DeskRecord,
  imported = true
)
open class DeskRecord(
  val id: DeskId,
  val name: DeskName
)
