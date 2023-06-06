package com.surely.quotes

import com.surely.TypeNames.com.surely.quotes.OccupationRatingFactor
import lang.taxi.annotations.DataType

@DataType(
  value = OccupationRatingFactor,
  imported = true
)
enum class OccupationRatingFactor {
  HighRisk,

  MediumRisk,

  LowRisk
}
