package com.surely

import kotlin.String

object TypeNames {
  object com {
    object surely {
      object addresses {
        const val HouseNumber: String = "com.surely.addresses.HouseNumber"

        const val StreetName: String = "com.surely.addresses.StreetName"

        const val Town: String = "com.surely.addresses.Town"

        const val PostCode: String = "com.surely.addresses.PostCode"

        const val Country: String = "com.surely.addresses.Country"
      }

      object quotes {
        const val RiskFactor: String = "com.surely.quotes.RiskFactor"

        const val OccupationRatingFactor: String = "com.surely.quotes.OccupationRatingFactor"

        const val NightParkingPostCodeRiskFactor: String =
            "com.surely.quotes.NightParkingPostCodeRiskFactor"

        const val ResidentialPostCodeRiskFactor: String =
            "com.surely.quotes.ResidentialPostCodeRiskFactor"

        const val VehicleRiskFactor: String = "com.surely.quotes.VehicleRiskFactor"

        const val NoClaimsDiscount: String = "com.surely.quotes.NoClaimsDiscount"

        const val CreditScore: String = "com.surely.quotes.CreditScore"

        const val OccupationCode: String = "com.surely.quotes.OccupationCode"

        const val OvernightParkingLocation: String = "com.surely.quotes.OvernightParkingLocation"

        const val QuoteId: String = "com.surely.quotes.QuoteId"

        const val AnnualPremium: String = "com.surely.quotes.AnnualPremium"

        object vehicles {
          const val LicensePlate: String = "com.surely.quotes.vehicles.LicensePlate"

          const val Manufacturer: String = "com.surely.quotes.vehicles.Manufacturer"

          const val ModelName: String = "com.surely.quotes.vehicles.ModelName"

          const val VehicleRiskFactor: String = "com.surely.quotes.vehicles.VehicleRiskFactor"

          const val OvernightParkingLocation: String =
              "com.surely.quotes.vehicles.OvernightParkingLocation"

          const val NightParkingPostCodeRiskFactor: String =
              "com.surely.quotes.vehicles.NightParkingPostCodeRiskFactor"
        }
      }
    }
  }
}
