package com.icycube.users

import com.icycube.TypeNames.com.icycube.users.Postcode
import kotlin.String
import lang.taxi.annotations.DataType

@DataType(
  value = Postcode,
  imported = true
)
typealias Postcode = String
