package com.icycube.posts

import com.icycube.TypeNames.com.icycube.posts.UserStatus
import kotlin.String
import lang.taxi.annotations.DataType

@DataType(
  value = UserStatus,
  imported = true
)
typealias UserStatus = String
