package films

import com.petflix.demos.TypeNames.films.Title
import kotlin.String
import lang.taxi.annotations.DataType

@DataType(
  value = Title,
  imported = true
)
typealias Title = String
