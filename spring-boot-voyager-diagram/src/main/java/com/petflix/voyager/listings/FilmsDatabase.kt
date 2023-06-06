package com.petflix.voyager.listings

import lang.taxi.annotations.DataType
import org.springframework.data.crossstore.ChangeSetPersister
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import javax.persistence.Entity
import javax.persistence.Id

@DataType
typealias FilmId = Int

@Entity
data class Film(
   @Id
   val filmId: FilmId,
   val title: String
)

interface FilmRepository : JpaRepository<Film, Int>

/**
 * A simple component to populate our database on startup
 */
@Component
class FilmDbSeeder(val repo: FilmRepository) {
   init {
      val films = listOf(
         Film(1, "Gladiator"),
         Film(2, "Star Wars IV - A New Hope"),
         Film(3, "Star Wars V - Empire Strikes Back"),
         Film(4, "Star Wars VI - Return of the Jedi"),
      )
      repo.saveAll(films)
   }
}

@RestController
class ListingsService(val repository: FilmRepository) {

   @GetMapping("/films")
   fun listAllFilms(): List<Film> = repository.findAll()

   @GetMapping("/films/{id}")
   fun getFilm(@PathVariable("id") filmId: FilmId): Film {
      return repository.findByIdOrNull(filmId) ?: throw ChangeSetPersister.NotFoundException()
   }
}
