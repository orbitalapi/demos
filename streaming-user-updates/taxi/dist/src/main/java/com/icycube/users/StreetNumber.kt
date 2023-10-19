package com.icycube.users

import com.icycube.TypeNames.com.icycube.users.StreetNumber
import kotlin.Int
import lang.taxi.annotations.DataType

@DataType(
  value = StreetNumber,
  imported = true
)
typealias StreetNumber = Int
