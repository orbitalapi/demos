package com.surely.quotes

import com.surely.TypeNames.com.surely.quotes.QuoteId
import kotlin.String
import lang.taxi.annotations.DataType

@DataType(
  value = QuoteId,
  imported = true
)
typealias QuoteId = String
