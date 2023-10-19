package com.orbitalhq.demos.icyqueue.api

import com.icycube.users.UUID
import com.orbitalhq.demos.icyqueue.User
import com.orbitalhq.demos.icyqueue.UserGenerator
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class UserService(private val userGenerator: UserGenerator) {

   private val usersById = userGenerator.users.associateBy { it.login.uuid }

   @GetMapping("/users")
   fun getAllUsers():List<User> = userGenerator.users

   @GetMapping("/users/{userId}")
   fun getUser(@PathVariable("userId") userId: UUID):User = usersById[userId] ?: error("No user with userId of $userId")
}
