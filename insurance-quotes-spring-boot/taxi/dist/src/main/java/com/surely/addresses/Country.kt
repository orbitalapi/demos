package com.surely.addresses

import com.surely.TypeNames.com.surely.addresses.Country
import kotlin.String
import lang.taxi.annotations.DataType

@DataType(
  value = Country,
  imported = true
)
typealias Country = String
