package com.lunarbank.demo

import com.lunarbank.TypeNames.com.lunarbank.demo.TraderName
import kotlin.String
import lang.taxi.annotations.DataType

@DataType(
  value = TraderName,
  imported = true
)
typealias TraderName = String
