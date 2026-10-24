package films

import com.petflix.demos.TypeNames.films.Length
import kotlin.String
import lang.taxi.annotations.DataType

@DataType(
  value = Length,
  imported = true
)
typealias Length = String
