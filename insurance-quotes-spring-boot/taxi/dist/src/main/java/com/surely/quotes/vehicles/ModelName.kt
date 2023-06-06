package com.surely.quotes.vehicles

import com.surely.TypeNames.com.surely.quotes.vehicles.ModelName
import kotlin.String
import lang.taxi.annotations.DataType

@DataType(
  value = ModelName,
  imported = true
)
typealias ModelName = String
