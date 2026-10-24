package films

import com.petflix.demos.TypeNames.films.Film
import lang.taxi.annotations.DataType

@DataType(
  value = Film,
  imported = true
)
open class Film(
  val film_id: FilmId,
  val title: Title,
  val description: Description,
  val release_year: ReleaseYear,
  val length: Length,
  val rating: Rating
)
