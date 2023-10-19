package com.icycube.users

import com.icycube.TypeNames.com.icycube.users.City
import kotlin.String
import lang.taxi.annotations.DataType

@DataType(
  value = City,
  imported = true
)
typealias City = String
