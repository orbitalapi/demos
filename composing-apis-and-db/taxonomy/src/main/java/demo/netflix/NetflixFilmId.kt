package demo.netflix

import com.petflix.demos.TypeNames.demo.netflix.NetflixFilmId
import kotlin.Int
import lang.taxi.annotations.DataType

@DataType(
  value = NetflixFilmId,
  imported = true
)
typealias NetflixFilmId = Int
