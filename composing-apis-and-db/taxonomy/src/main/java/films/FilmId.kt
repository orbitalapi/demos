package films

import com.petflix.demos.TypeNames.films.FilmId
import kotlin.Int
import lang.taxi.annotations.DataType

@DataType(
  value = FilmId,
  imported = true
)
typealias FilmId = Int
