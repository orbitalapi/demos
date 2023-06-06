package com.surely.addresses

import com.surely.TypeNames.com.surely.addresses.Town
import kotlin.String
import lang.taxi.annotations.DataType

@DataType(
  value = Town,
  imported = true
)
typealias Town = String
