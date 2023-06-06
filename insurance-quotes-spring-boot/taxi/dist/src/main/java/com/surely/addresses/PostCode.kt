package com.surely.addresses

import com.surely.TypeNames.com.surely.addresses.PostCode
import kotlin.String
import lang.taxi.annotations.DataType

@DataType(
  value = PostCode,
  imported = true
)
typealias PostCode = String
