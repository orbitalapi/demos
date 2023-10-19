package com.orbitalhq.demos.icyqueue

import com.fasterxml.jackson.annotation.JsonProperty
import com.icycube.users.*

data class UserResponse(
   val results: List<User>,
   val info: Info
)

data class User(
   val gender: Gender,
   val name: Name,
   val location: Location,
   val email: String,
   val login: Login,
   val dob: Dob,
   @JsonProperty("registered")
   val registration: Registered,
   val phone: String,
   val cell: String,
   val id: Id,
   val picture: Picture,
   val nat: String
)

data class Name(
   val title: Title,
   val first: FirstName,
   val last: LastName
)

data class Location(
   val street: Street,
   val city: String,
   val state: String,
   val country: String,
   val postcode: Any,  // This can be a String or Int based on the country
   val coordinates: Coordinates,
   val timezone: Timezone
)

data class Street(
   val number: Int,
   val name: String
)

data class Coordinates(
   val latitude: String,
   val longitude: String
)

data class Timezone(
   val offset: String,
   val description: String
)

data class Login(
   val uuid: UUID,
   val username: Username,
   val password: String,
   val salt: String,
   val md5: String,
   val sha1: String,
   val sha256: String
)

data class Dob(
   val date: String,
   val age: Int
)

data class Registered(
   val date: String,
   val age: Int
)

data class Id(
   val name: String,
   val value: String?
)

data class Picture(
   val large: LargePictureURL,
   val medium: MediumPictureURL,
   val thumbnail: ThumbnailPictureURL
)

data class Info(
   val seed: String,
   val results: Int,
   val page: Int,
   val version: String
)
