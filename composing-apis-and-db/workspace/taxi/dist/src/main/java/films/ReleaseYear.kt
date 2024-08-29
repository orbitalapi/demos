package films

import com.petflix.demos.TypeNames.films.ReleaseYear
import kotlin.String
import lang.taxi.annotations.DataType

@DataType(
  value = ReleaseYear,
  imported = true
)
typealias ReleaseYear = String
