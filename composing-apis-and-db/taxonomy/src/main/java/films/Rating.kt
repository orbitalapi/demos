package films

import com.petflix.demos.TypeNames.films.Rating
import kotlin.String
import lang.taxi.annotations.DataType

@DataType(
  value = Rating,
  imported = true
)
typealias Rating = String
