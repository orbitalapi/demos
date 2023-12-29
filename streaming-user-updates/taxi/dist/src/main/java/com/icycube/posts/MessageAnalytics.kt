package com.icycube.posts

import com.icycube.TypeNames.com.icycube.posts.MessageAnalytics
import lang.taxi.annotations.DataType

@DataType(
  value = MessageAnalytics,
  imported = true
)
open class MessageAnalytics(
  val id: MessageId,
  val views: ViewCount
)
