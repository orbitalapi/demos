package com.orbitalhq.demos.icyqueue

import lang.taxi.annotations.DataType
import java.util.concurrent.atomic.AtomicInteger

private val statuses: List<String> = listOf(
   "In a meeting",
   "Offline",
   "At lunch",
   "On a call",
   "Busy",
   "Available",
   "Out of office",
   "On vacation",
   "Working remotely",
   "In a workshop",
   "Plotting world domination",
   "Sharpening my lightsaber",
   "Fighting crime",
   "Invisible",
   "Training at the superhero academy",
   "At the Batcave",
   "In a secret lair",
   "In an intergalactic council",
   "Negotiating with aliens",
   "Rescuing kittens",
   "Defusing a bomb",
   "Chasing a villain",
   "In a high-speed chase",
   "On a space adventure",
   "Battling a dragon",
   "In a magic duel",
   "Casting spells",
   "In a quidditch match",
   "Chasing the Snitch",
   "At the Shire",
   "Finding the One Ring",
   "Hiding from dementors",
   "Brewing potions",
   "In a lightsaber duel",
   "Leading a rebellion",
   "On a stealth mission",
   "In a galaxy far, far away",
   "Time traveling",
   "On a quest",
   "Hunting horcruxes",
   "At a wizard duel",
   "Escaping from Azkaban",
   "Attending a superhero summit",
   "On a starship voyage",
   "Negotiating peace treaties",
   "At Stark Industries",
   "In a Jedi council",
   "On a reconnaissance mission",
   "Defending the realm",
   "At the Fortress of Solitude",
   "Undercover",
   "On a mystical quest",
   "In a parallel universe",
   "In cryogenic sleep",
   "In the danger room"
)

@DataType("com.orbitalhq.films.announcements.UserStatusUpdateMessage", imported = true)
data class UserStatusUpdateMessage(
   val messageId: Int,
   val userUuid: String,
   val status: String
)

object StatusGenerator {
   private val counter = AtomicInteger(0)
   fun generateRandomStatusUpdate(users: List<User>): UserStatusUpdateMessage {
      val user = users.random()
      val message = statuses.random()

      return UserStatusUpdateMessage(counter.incrementAndGet(), user.login.uuid, message)
   }
}


data class MessageAnalytics(
   val id: Int,
   val views: Int
) {
   fun increaseViewCount(increasedViewCount: Int):MessageAnalytics {
      return this.copy(views = this.views +increasedViewCount)
   }
}
