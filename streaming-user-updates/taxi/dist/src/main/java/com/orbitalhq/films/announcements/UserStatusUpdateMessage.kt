package com.orbitalhq.films.announcements

import com.icycube.TypeNames.com.orbitalhq.films.announcements.UserStatusUpdateMessage
import com.icycube.users.UUID
import lang.taxi.annotations.DataType

@DataType(
  value = UserStatusUpdateMessage,
  imported = true
)
open class UserStatusUpdateMessage(
  val userUuid: UUID,
  val status: UserStatus
)
