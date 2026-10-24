package com.petflix.v2.listings

import films.FilmId
import films.StreamingProviderName
import films.StreamingProviderPrice
import lang.taxi.annotations.*
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
@Service
class StreamingMoviesProvider {

    data class StreamingProvider(
        val name: StreamingProviderName,
        val pricePerMonth: StreamingProviderPrice
    )

    private val streamingProviders: List<StreamingProvider> = listOf(
        StreamingProvider(name = "Netflix", pricePerMonth = 9.99.toBigDecimal()),
        StreamingProvider(name = "Disney Plus", pricePerMonth = 7.99.toBigDecimal()),
        StreamingProvider(name = "Now TV", pricePerMonth = 13.99.toBigDecimal()),
        StreamingProvider(name = "Hulu", pricePerMonth = 8.99.toBigDecimal()),
    )

    @Operation
    @GetMapping("/films/{filmId}/streamingProviders")
    fun getStreamingProvidersForFilm(@PathVariable("filmId") filmId: FilmId): StreamingProvider {
        return streamingProviders.random()
    }

    @Operation
    @PostMapping("/films/streamingServices")
    fun findStreamingProvidersForFilm(@RequestBody request: StreamingProviderRequest): StreamingProvider {
        return streamingProviders.random()
    }

    @ParameterType
    @DataType
    data class StreamingProviderRequest(
        val filmId: FilmId
    )
}
