package com.lunarbank.demo

import com.lunarbank.TypeNames.com.lunarbank.demo.IssuerName
import kotlin.String
import lang.taxi.annotations.DataType

@DataType(
  value = IssuerName,
  imported = true
)
typealias IssuerName = String
