package com.icycube.users

import com.icycube.TypeNames.com.icycube.users.PhoneNumber
import kotlin.String
import lang.taxi.annotations.DataType

@DataType(
  value = PhoneNumber,
  imported = true
)
typealias PhoneNumber = String
