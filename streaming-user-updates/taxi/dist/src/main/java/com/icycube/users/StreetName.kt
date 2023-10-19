package com.icycube.users

import com.icycube.TypeNames.com.icycube.users.StreetName
import kotlin.String
import lang.taxi.annotations.DataType

@DataType(
  value = StreetName,
  imported = true
)
typealias StreetName = String
