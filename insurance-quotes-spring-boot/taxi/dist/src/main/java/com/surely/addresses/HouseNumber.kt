package com.surely.addresses

import com.surely.TypeNames.com.surely.addresses.HouseNumber
import kotlin.Int
import lang.taxi.annotations.DataType

@DataType(
  value = HouseNumber,
  imported = true
)
typealias HouseNumber = Int
