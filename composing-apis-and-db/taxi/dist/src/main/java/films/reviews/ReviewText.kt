package films.reviews

import com.petflix.demos.TypeNames.films.reviews.ReviewText
import kotlin.String
import lang.taxi.annotations.DataType

@DataType(
  value = ReviewText,
  imported = true
)
typealias ReviewText = String
