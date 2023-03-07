package com.petflix.films

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal

@RestController
class StreamingMoviesProvider {

    data class StreamingProvider(
        val name: String,
        val pricePerMonth: BigDecimal
    )

    private val streamingProviders: List<StreamingProvider> = listOf(
        StreamingProvider(name = "Netflix", pricePerMonth = 9.99.toBigDecimal()),
        StreamingProvider(name = "Disney Plus", pricePerMonth = 7.99.toBigDecimal()),
        StreamingProvider(name = "Now TV", pricePerMonth = 13.99.toBigDecimal()),
        StreamingProvider(name = "Hulu", pricePerMonth = 8.99.toBigDecimal()),
    )

    @GetMapping("/films/{filmId}/streamingProviders")
    fun getStreamingProvidersForFilm(@PathVariable("filmId") filmId: Int): StreamingProvider {
        return streamingProviders.random()
    }

//
////
//
//    private val streamingProviders: List<StreamingProvider> = listOf(
//        StreamingProvider(
//            service = "Netflix",
//            costs = StreamingServiceCosts(
//                monthlyCost = 9.99.toBigDecimal(),
//                cancellationFee = 29.99.toBigDecimal(),
//                annualCost = 99.99.toBigDecimal()
//            )
//        ),
//        StreamingProvider(
//            service = "Disney Plus",
//            costs = StreamingServiceCosts(
//                monthlyCost = 7.99.toBigDecimal(),
//                cancellationFee = 0.toBigDecimal(),
//                annualCost = 79.99.toBigDecimal()
//            )
//        ),
//        StreamingProvider(
//            service = "Now TV",
//            costs = StreamingServiceCosts(
//                monthlyCost = 13.99.toBigDecimal(),
//                cancellationFee = 5.99.toBigDecimal(),
//                annualCost = 129.99.toBigDecimal()
//            )
//        ),
//        StreamingProvider(
//            service = "Hulu",
//            costs = StreamingServiceCosts(
//                monthlyCost = 8.99.toBigDecimal(),
//                cancellationFee = 23.99.toBigDecimal(),
//                annualCost = 129.99.toBigDecimal()
//            )
//        ),
//    )
//
//    data class StreamingProvider(
//        val service: StreamingProviderName,
//        val costs: StreamingServiceCosts
//    )
//
//    data class StreamingServiceCosts(
//        val monthlyCost: StreamingProviderPrice,
//        val cancellationFee: BigDecimal,
//        val annualCost: BigDecimal
//    )
//
//



//
//    @Operation
//    @PostMapping("/films/streamingServices")
//    fun getStreamingProvidersForFilm(@RequestBody request: StreamingProviderRequest): StreamingProvider {
//        return streamingProviders.random()
//    }
//
//    @ParameterType
//    @DataType
//    data class StreamingProviderRequest(
//        val filmId: FilmId
//    )



























    //
//    @Operation
//    @PostMapping("/films/streamingProviders")
//    fun getBulkStreamingProvidersForFilm(@RequestBody request: StreamingProvidersBulkLookupRequest): List<StreamingProvidersBulkLookupResponse> {
//        return request.filmIds.map { filmId ->
//            StreamingProvidersBulkLookupResponse(
//                filmId,
//                streamingProviders.random()
//            )
//        }
//
//    }
//
//    data class StreamingProvidersBulkLookupRequest(
//        val filmIds: List<FilmId>
//    )
//
//    data class StreamingProvidersBulkLookupResponse(
//        val filmId: FilmId,
//        val provider: StreamingProvider
//    )
}




