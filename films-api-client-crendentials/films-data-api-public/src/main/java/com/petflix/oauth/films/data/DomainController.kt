package com.petflix.oauth.films.data

import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class DomainController(private val domainService: DomainService)  {
    @Tag(name = "CastData", description = "Cast Information.")
    @GetMapping("/cast/{id}")
    fun getCast(@PathVariable id:String) = domainService.getCast(id)

    @Tag(name = "DirectorData", description = "Director Information.")
    @GetMapping("/director/{id}")
    fun getDirector(@PathVariable id: String) = domainService.getDirectors(id)

    @Tag(name = "WriterData", description = "Writer Information.")
    @GetMapping("/writer/{id}")
    fun getWriter(@PathVariable id: String) = domainService.getWriters(id)
}