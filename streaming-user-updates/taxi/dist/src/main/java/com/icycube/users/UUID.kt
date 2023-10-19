package com.icycube.users

import com.icycube.TypeNames.com.icycube.users.UUID
import kotlin.String
import lang.taxi.annotations.DataType

@DataType(
  value = UUID,
  imported = true
)
typealias UUID = String
