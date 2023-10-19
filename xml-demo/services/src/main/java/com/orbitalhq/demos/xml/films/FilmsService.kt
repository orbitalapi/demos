package com.orbitalhq.demos.xml.films

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class FilmsService {

   @GetMapping("/films", produces = ["application/xml"])
   fun listFilms(): List<Film> {
      return FilmData.films
   }

   @GetMapping("/film/{filmId}/cast", produces = ["application/xml"])
   fun getCast(@PathVariable("filmId") filmId: String): List<Actor> {
      val cast = (0..10).map { FilmData.actors.random() }
      return cast
   }

   @GetMapping("/film/{filmId}/awards", produces = ["application/json"])
   fun getAwards(@PathVariable("filmId") filmInt: String): List<Award> = (0..3).map { FilmData.movieAwards.random() }
}
