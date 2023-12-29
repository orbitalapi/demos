package com.lunarbank.demo

import com.lunarbank.TypeNames.com.lunarbank.demo.InstrumentName
import kotlin.String
import lang.taxi.annotations.DataType

@DataType(
  value = InstrumentName,
  imported = true
)
typealias InstrumentName = String
