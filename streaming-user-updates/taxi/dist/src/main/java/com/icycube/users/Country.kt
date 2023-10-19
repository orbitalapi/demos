package com.icycube.users

import com.icycube.TypeNames.com.icycube.users.Country
import kotlin.String
import lang.taxi.annotations.DataType

@DataType(
  value = Country,
  imported = true
)
typealias Country = String
