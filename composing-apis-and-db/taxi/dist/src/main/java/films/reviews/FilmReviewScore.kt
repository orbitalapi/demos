package films.reviews

import com.petflix.demos.TypeNames.films.reviews.FilmReviewScore
import java.math.BigDecimal
import lang.taxi.annotations.DataType

@DataType(
  value = FilmReviewScore,
  imported = true
)
typealias FilmReviewScore = BigDecimal
