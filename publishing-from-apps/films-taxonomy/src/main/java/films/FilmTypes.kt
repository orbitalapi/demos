package films

import lang.taxi.annotations.DataType
import java.math.BigDecimal

// Declared in ../../../../../../taxi/src/films/films.taxi

@DataType(value = "films.FilmId", imported = true)
typealias FilmId = Int

@DataType(value = "films.StreamingProviderName", imported = true)
typealias StreamingProviderName = String

@DataType(value = "films.StreamingProviderPrice", imported = true)
typealias StreamingProviderPrice = BigDecimal
