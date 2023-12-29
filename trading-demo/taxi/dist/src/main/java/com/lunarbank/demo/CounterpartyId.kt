package com.lunarbank.demo

import com.lunarbank.TypeNames.com.lunarbank.demo.CounterpartyId
import kotlin.String
import lang.taxi.annotations.DataType

@DataType(
  value = CounterpartyId,
  imported = true
)
typealias CounterpartyId = String
