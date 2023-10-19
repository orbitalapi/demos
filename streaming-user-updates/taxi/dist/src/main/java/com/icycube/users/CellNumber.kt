package com.icycube.users

import com.icycube.TypeNames.com.icycube.users.CellNumber
import kotlin.String
import lang.taxi.annotations.DataType

@DataType(
  value = CellNumber,
  imported = true
)
typealias CellNumber = String
