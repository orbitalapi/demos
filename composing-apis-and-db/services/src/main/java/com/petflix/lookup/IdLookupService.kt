package com.petflix.lookup

import demo.netflix.NetflixFilmId
import films.FilmId
import films.reviews.SquashedTomatoesFilmId
import lang.taxi.annotations.Operation
import lang.taxi.annotations.Service
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
@Service
class IdLookupService {

    @GetMapping("/ids/squashed/{id}")
    @Operation
    fun lookupFromSquashedTomatoesId(@PathVariable("id") id: SquashedTomatoesFilmId): IdResolution =
        lookupFromId(id)

    @GetMapping("/ids/internal/{id}")
    @Operation
    fun lookupFromInternalFilmId(@PathVariable("id") id: FilmId): IdResolution =
        lookupFromId(id.toString())

    @GetMapping("/ids/netflix/{id}")
    @Operation
    fun lookupFromNetflixFilmId(@PathVariable("id") id: NetflixFilmId): IdResolution =
        lookupFromId((id - 1000).toString())


    private fun lookupFromId(id: String): IdResolution {
        val filmId = id.split("-").last().toInt()
        return IdResolution(filmId)
    }
}

data class IdResolution(
    val filmId: FilmId,
    val netflixId: NetflixFilmId = filmId + 1000,
    val squashedTomatoesFilmId: SquashedTomatoesFilmId = "squashed-$filmId"
)
