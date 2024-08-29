package films

import com.petflix.demos.TypeNames.films.Description
import kotlin.String
import lang.taxi.annotations.DataType

@DataType(
  value = Description,
  imported = true
)
typealias Description = String
