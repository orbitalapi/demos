package com.lunarbank.demo

import com.lunarbank.TypeNames.com.lunarbank.demo.DeskName
import kotlin.String
import lang.taxi.annotations.DataType

@DataType(
  value = DeskName,
  imported = true
)
typealias DeskName = String
