package com.icycube.posts

import com.icycube.TypeNames.com.icycube.posts.MessageId
import kotlin.Int
import lang.taxi.annotations.DataType

@DataType(
  value = MessageId,
  imported = true
)
typealias MessageId = Int
