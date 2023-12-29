package com.lunarbank.demo

import com.lunarbank.TypeNames.com.lunarbank.demo.DeskId
import kotlin.Int
import lang.taxi.annotations.DataType

@DataType(
  value = DeskId,
  imported = true
)
typealias DeskId = Int
