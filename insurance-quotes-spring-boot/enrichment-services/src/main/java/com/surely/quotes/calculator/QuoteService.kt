package com.surely.quotes.calculator

import com.orbital.client.OrbitalTransport
import com.orbital.client.converter.run
import com.orbital.client.given
import com.surely.quotes.taxonomy.*
import lang.taxi.annotations.DataType
import lang.taxi.annotations.Operation
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

@RestController
class QuoteService(private val orbitalClient: OrbitalTransport) {

   @PostMapping("/quotes")
   @Operation(excluded = true)
   fun calculatePremium(@RequestBody quoteRequest: QuoteRequest): Mono<InsurancePremiumQuote> {
      return  given(quoteRequest)
         .find<InsurancePremiumQuote>()
         .run(orbitalClient)
         .toMono()
   }
}

/**
 * An example quote request, probably sent from a UI somewhere.
 */
@DataType("QuoteRequest")
data class QuoteRequest(
   val quoteId: QuoteId,
   val occupation: OccupationCode,
   val parkingLocation: OvernightParkingLocation,
   val postcode: PostCode,
   val licensePlate: CarLicensePlate
)
