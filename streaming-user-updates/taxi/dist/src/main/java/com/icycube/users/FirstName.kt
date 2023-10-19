package com.icycube.users

import com.icycube.TypeNames.com.icycube.users.FirstName
import kotlin.String
import lang.taxi.annotations.DataType

@DataType(
  value = FirstName,
  imported = true
)
typealias FirstName = String
