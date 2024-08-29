package com.petflix.demos

import kotlin.String

object TypeNames {
  object demo {
    object netflix {
      const val NetflixFilmId: String = "demo.netflix.NetflixFilmId"
    }
  }

  object films {
    const val StreamingProviderName: String = "films.StreamingProviderName"

    const val StreamingProviderPrice: String = "films.StreamingProviderPrice"

    const val FilmId: String = "films.FilmId"

    const val Description: String = "films.Description"

    const val Title: String = "films.Title"

    const val Rating: String = "films.Rating"

    const val ReleaseYear: String = "films.ReleaseYear"

    const val Film: String = "films.Film"

    const val Length: String = "films.Length"

    object reviews {
      const val SquashedTomatoesFilmId: String = "films.reviews.SquashedTomatoesFilmId"

      const val FilmReviewScore: String = "films.reviews.FilmReviewScore"

      const val ReviewText: String = "films.reviews.ReviewText"
    }
  }
}
