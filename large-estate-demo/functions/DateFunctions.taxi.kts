package com.acme.functions

import com.orbitalhq.functions.TaxiFunction
import com.orbitalhq.functions.TaxiParam
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@TaxiFunction(description = "The number of whole days from `start` to `end`. Negative when `end` is before `start`.")
fun daysBetween(@TaxiParam start: LocalDate, @TaxiParam end: LocalDate): Int =
   ChronoUnit.DAYS.between(start, end).toInt()
