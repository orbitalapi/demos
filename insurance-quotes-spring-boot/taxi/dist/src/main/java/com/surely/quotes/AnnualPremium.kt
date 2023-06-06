package com.surely.quotes

import com.surely.TypeNames.com.surely.quotes.AnnualPremium
import java.math.BigDecimal
import lang.taxi.annotations.DataType

@DataType(
  value = AnnualPremium,
  imported = true
)
typealias AnnualPremium = BigDecimal
