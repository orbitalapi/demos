package com.orbitalhq.demos.icyqueue.api

import com.icycube.TypeNames
import com.orbitalhq.demos.icyqueue.UserStatusUpdateMessage
import com.orbitalhq.demos.icyqueue.UserUpdatePublisher
import lang.taxi.annotations.DataType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController


@DataType(
   value = "com.orbitalhq.films.announcements.MessageId",
   imported = true
)
typealias MessageId = Int


@RestController
class TweetService(private val updatePublisher: UserUpdatePublisher) {


   @GetMapping("/messages/{messageId}")
   fun getMessage(@PathVariable("messageId") messageId: MessageId):UserStatusUpdateMessage {
      return updatePublisher.getPostById(messageId) ?: error("Not found")
   }


}
