package com.surely.quotes

import com.surely.TypeNames.com.surely.quotes.NoClaimsDiscount
import java.math.BigDecimal
import lang.taxi.annotations.DataType

@DataType(
  value = NoClaimsDiscount,
  imported = true
)
typealias NoClaimsDiscount = BigDecimal
