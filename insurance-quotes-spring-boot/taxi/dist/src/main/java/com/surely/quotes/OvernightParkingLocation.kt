package com.surely.quotes

import com.surely.TypeNames.com.surely.quotes.OvernightParkingLocation
import lang.taxi.annotations.DataType

@DataType(
  value = OvernightParkingLocation,
  imported = true
)
enum class OvernightParkingLocation {
  Garage,

  Street,

  OffStreet
}
