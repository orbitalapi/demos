package films

import com.petflix.demos.TypeNames.films.StreamingProviderPrice
import java.math.BigDecimal
import lang.taxi.annotations.DataType

@DataType(
  value = StreamingProviderPrice,
  imported = true
)
typealias StreamingProviderPrice = BigDecimal
