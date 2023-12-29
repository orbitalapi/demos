package com.lunarbank.demo

import com.lunarbank.TypeNames.com.lunarbank.demo.EsgRating
import lang.taxi.annotations.DataType

@DataType(
  value = EsgRating,
  imported = true
)
open class EsgRating(
  val isin: Isin,
  val environmentalPillarScore: EnvironmentalPillarScore,
  val socialPillarScore: SocialPillarScore,
  val governancePillarScore: GovernancePillarScore,
  val carbonIntensityWeightedAverage: CarbonIntensityWeightedAverage
)
