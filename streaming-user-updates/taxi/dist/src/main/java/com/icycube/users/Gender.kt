package com.icycube.users

import com.icycube.TypeNames.com.icycube.users.Gender
import kotlin.String
import lang.taxi.annotations.DataType

@DataType(
  value = Gender,
  imported = true
)
typealias Gender = String
