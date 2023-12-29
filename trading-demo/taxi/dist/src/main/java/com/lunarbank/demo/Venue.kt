package com.lunarbank.demo

import com.lunarbank.TypeNames.com.lunarbank.demo.Venue
import kotlin.String
import lang.taxi.annotations.DataType

@DataType(
  value = Venue,
  imported = true
)
typealias Venue = String
