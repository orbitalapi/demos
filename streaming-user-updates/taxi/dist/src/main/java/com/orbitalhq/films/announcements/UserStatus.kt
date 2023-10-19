package com.orbitalhq.films.announcements

import com.icycube.TypeNames.com.orbitalhq.films.announcements.UserStatus
import kotlin.String
import lang.taxi.annotations.DataType

@DataType(
  value = UserStatus,
  imported = true
)
typealias UserStatus = String
