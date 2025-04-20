package com.petflix.oauth.films.data

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Service

@Service
class DomainService(private val mapper: ObjectMapper) {
    private val castMap = mapper.readValue(ClassPathResource("cast.json").inputStream.buffered(), object: TypeReference<Map<String, String>>() {}).map {
        Cast(it.key, it.value)
    }.associateBy {
        it.id
    }

    private val directorMap = mapper.readValue(ClassPathResource("directors.json").inputStream.buffered(), object: TypeReference<Map<String, String>>() {}).map {
        Director(it.key, it.value)
    }.associateBy { it.id}

    private val writerMap = mapper.readValue(ClassPathResource("writers.json").inputStream.buffered(), object: TypeReference<Map<String, String>>() {}).map {
        Writer(it.key, it.value)
    }.associateBy { it.id }

    fun getCast(id:String) = castMap[id]
    fun getDirectors(id:String) = directorMap[id]
    fun getWriters(id:String) = writerMap[id]
}