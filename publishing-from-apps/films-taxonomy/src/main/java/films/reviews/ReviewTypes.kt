package films.reviews

import lang.taxi.annotations.DataType
import java.math.BigDecimal

// Declared in ../../../../../../../taxi/src/films/reviews.taxi

@DataType(value = "films.reviews.SquashedTomatoesFilmId", imported = true)
typealias SquashedTomatoesFilmId = String

@DataType(value = "films.reviews.FilmReviewScore", imported = true)
typealias FilmReviewScore = BigDecimal

@DataType(value = "films.reviews.ReviewText", imported = true)
typealias ReviewText = String
