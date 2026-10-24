package films

import com.petflix.demos.TypeNames.films.StreamingProviderName
import kotlin.String
import lang.taxi.annotations.DataType

@DataType(
  value = StreamingProviderName,
  imported = true
)
typealias StreamingProviderName = String
