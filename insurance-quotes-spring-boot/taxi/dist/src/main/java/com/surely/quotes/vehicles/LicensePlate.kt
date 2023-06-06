package com.surely.quotes.vehicles

import com.surely.TypeNames.com.surely.quotes.vehicles.LicensePlate
import kotlin.String
import lang.taxi.annotations.DataType

@DataType(
  value = LicensePlate,
  imported = true
)
typealias LicensePlate = String
