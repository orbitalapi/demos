package com.orbitalhq.demos.icyqueue

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.stereotype.Component
import java.nio.file.Paths

@Component
class UserGenerator {
   val users:List<User>
   init {
      val path = Paths.get(ClassLoader.getSystemResource("users.json").toURI())
      val userResponse = jacksonObjectMapper()
         .readValue<UserResponse>(path.toFile())
      users = userResponse.results
   }
}
