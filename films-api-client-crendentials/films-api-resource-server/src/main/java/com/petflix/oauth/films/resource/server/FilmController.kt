package com.petflix.oauth.films.resource.server

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/films")
class FilmController(private val filmsService: FilmsService) {
    @Operation(summary = "Top 100 IMDB Movies",  description = "Retrieves Top 100 Imdb Movies.",
        security = [SecurityRequirement(name = "security_auth")])
    @Tag(name = "topImdbMovies", description = "Top 100 Imdb Movies.")
    @GetMapping("/")
    fun getFilms() = filmsService.getFilms()

    @Operation(summary = "A Top 100 Imdb Movie Detail",  description = "Retrieves Details of a Top 100 Imdb Movie.",
        security = [SecurityRequirement(name = "security_auth")])
    @Tag(name = "findFilm", description = "Find the film for the given Id.")
    @GetMapping("/{id}")
    fun getFilm(@PathVariable id:String) = filmsService.getFilm(id)
}