package com.petflix.voyager.listings

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import kotlin.random.Random


@Entity
data class Review(
   val filmId: FilmId,
   val reviewText: String,
   val score: Double,

   @Id @GeneratedValue(strategy = GenerationType.AUTO) val id: Int?,
)

interface ReviewRepository : JpaRepository<Review, Int> {
   fun findAllByFilmId(filmId: Int): Review
}

@RestController
class ReviewService(val repo: ReviewRepository) {

   init {
      repo.saveAll(listOf(1, 2, 3, 4, 5).map { filmId ->
         Review(
            filmId,
            listOf("Good", "Great", "Not bad", "Ok", "Terrible").random(),
            score = Random.nextDouble(3.0, 5.0),
            null
         )
      })
   }

   @GetMapping("/reviews")
   fun listAll(): List<Review> {
      return repo.findAll()
   }

   @GetMapping("/reviews/{filmId}")
   fun getReviewsForFilm(@PathVariable("filmId") filmId: FilmId): Review {
      return repo.findAllByFilmId(filmId)
   }
}
