package com.surely.quotes.taxonomy

import lang.taxi.annotations.DataType
import java.math.BigDecimal


@DataType
enum class OvernightParkingLocation {
   Garage,

   Street,

   OffStreet
}

@DataType
typealias NightParkingPostCodeRiskFactor = RiskFactor

@DataType
typealias OccupationRatingFactor = RiskFactor

@DataType
typealias VehicleRiskFactor = RiskFactor

@DataType
typealias OccupationCode = String

@DataType
typealias ModelName = String

@DataType
typealias CarLicensePlate = String

enum class RiskFactor {
   HighRisk,

   MediumRisk,

   LowRisk
}

@DataType
typealias PostCode = String

@DataType
enum class Manufacturer {
   Volvo,

   Audi,

   BMW,

   Nissan,

   Toyota
}

@DataType
typealias QuoteId = String

@DataType
typealias AnnualPremium = BigDecimal
