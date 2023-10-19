package com.icycube.users

import com.icycube.TypeNames.com.icycube.users.DateString
import kotlin.String
import lang.taxi.annotations.DataType

@DataType(
  value = DateString,
  imported = true
)
typealias DateString = String
