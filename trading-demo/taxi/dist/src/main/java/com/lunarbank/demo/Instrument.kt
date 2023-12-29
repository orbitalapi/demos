package com.lunarbank.demo

import com.lunarbank.TypeNames.com.lunarbank.demo.Instrument
import lang.taxi.annotations.DataType

@DataType(
  value = Instrument,
  imported = true
)
open class Instrument(
  val id: Isin,
  val name: InstrumentName,
  val maturityDate: MaturityDate,
  val issuerName: IssuerName
)
