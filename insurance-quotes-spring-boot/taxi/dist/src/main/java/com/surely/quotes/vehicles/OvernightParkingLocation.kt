package com.surely.quotes.vehicles

import com.surely.TypeNames.com.surely.quotes.vehicles.OvernightParkingLocation
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
