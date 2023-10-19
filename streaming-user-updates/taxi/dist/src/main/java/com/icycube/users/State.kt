package com.icycube.users

import com.icycube.TypeNames.com.icycube.users.State
import kotlin.String
import lang.taxi.annotations.DataType

@DataType(
  value = State,
  imported = true
)
typealias State = String
