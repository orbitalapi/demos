package com.lunarbank.demo

import com.lunarbank.TypeNames.com.lunarbank.demo.Isin
import kotlin.String
import lang.taxi.annotations.DataType

@DataType(
  value = Isin,
  imported = true
)
typealias Isin = String
