package com.surely.quotes.vehicles

import com.surely.TypeNames.com.surely.quotes.vehicles.Manufacturer
import lang.taxi.annotations.DataType

@DataType(
  value = Manufacturer,
  imported = true
)
enum class Manufacturer {
  Volvo,

  Audi,

  BMW,

  Nissan,

  Toyota
}
