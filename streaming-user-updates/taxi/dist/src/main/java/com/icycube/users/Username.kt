package com.icycube.users

import com.icycube.TypeNames.com.icycube.users.Username
import kotlin.String
import lang.taxi.annotations.DataType

@DataType(
  value = Username,
  imported = true
)
typealias Username = String
