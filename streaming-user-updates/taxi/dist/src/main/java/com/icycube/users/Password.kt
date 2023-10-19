package com.icycube.users

import com.icycube.TypeNames.com.icycube.users.Password
import kotlin.String
import lang.taxi.annotations.DataType

@DataType(
  value = Password,
  imported = true
)
typealias Password = String
