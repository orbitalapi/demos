package com.surely.quotes

import com.surely.TypeNames.com.surely.quotes.CreditScore
import kotlin.Int
import lang.taxi.annotations.DataType

@DataType(
  value = CreditScore,
  imported = true
)
typealias CreditScore = Int
