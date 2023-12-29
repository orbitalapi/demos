package com.lunarbank.demo

import com.lunarbank.TypeNames.com.lunarbank.demo.MaturityDate
import java.time.LocalDate
import lang.taxi.annotations.DataType

@DataType(
  value = MaturityDate,
  imported = true
)
typealias MaturityDate = LocalDate
