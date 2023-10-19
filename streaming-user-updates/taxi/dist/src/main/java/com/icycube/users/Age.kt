package com.icycube.users

import com.icycube.TypeNames.com.icycube.users.Age
import kotlin.Int
import lang.taxi.annotations.DataType

@DataType(
  value = Age,
  imported = true
)
typealias Age = Int
