package com.petflix.oauth.films.resource.server

import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/films")
class FilmController(private val filmsService: FilmsService) {
    @Tag(name = "topImdbMovies", description = "Top 100 Imdb Movies.")
    @GetMapping("/")
    fun getFilms() = filmsService.getFilms()

    @Tag(name = "findFilm", description = "Find the film for the given Id.")
    @GetMapping("/{id}")
    fun getFilm(@PathVariable id:String) = filmsService.getFilm(id)
}