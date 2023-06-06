package com.surely.addresses

import com.surely.TypeNames.com.surely.addresses.StreetName
import kotlin.String
import lang.taxi.annotations.DataType

@DataType(
  value = StreetName,
  imported = true
)
typealias StreetName = String
