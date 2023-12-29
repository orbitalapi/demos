package com.icycube.posts

import com.icycube.TypeNames.com.icycube.posts.UserStatusUpdateMessage
import com.icycube.users.UUID
import lang.taxi.annotations.DataType

@DataType(
  value = UserStatusUpdateMessage,
  imported = true
)
open class UserStatusUpdateMessage(
  val messageId: MessageId,
  val userUuid: UUID,
  val status: UserStatus
)
