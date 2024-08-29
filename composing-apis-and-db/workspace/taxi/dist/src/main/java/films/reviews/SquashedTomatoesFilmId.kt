package films.reviews

import com.petflix.demos.TypeNames.films.reviews.SquashedTomatoesFilmId
import kotlin.String
import lang.taxi.annotations.DataType

@DataType(
  value = SquashedTomatoesFilmId,
  imported = true
)
typealias SquashedTomatoesFilmId = String
