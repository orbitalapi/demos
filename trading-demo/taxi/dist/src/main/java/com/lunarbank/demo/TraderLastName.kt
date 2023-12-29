package com.lunarbank.demo

import com.lunarbank.TypeNames.com.lunarbank.demo.TraderLastName
import kotlin.String
import lang.taxi.annotations.DataType

@DataType(
  value = TraderLastName,
  imported = true
)
typealias TraderLastName = String
