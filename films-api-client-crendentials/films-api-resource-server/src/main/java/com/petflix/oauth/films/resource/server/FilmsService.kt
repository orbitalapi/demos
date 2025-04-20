package com.petflix.oauth.films.resource.server

import org.apache.commons.csv.CSVFormat
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Service

@Service
class FilmsService {
    private val films = CSVFormat.DEFAULT
        .builder()
        .setHeader("rank","id","name","year","imdb_votes","imdb_rating","certificate","duration","genre","cast_id","cast_name","director_id","director_name","writer_name","writer_id","img_link")
        .setSkipHeaderRecord(true)
        .get()
        .parse(ClassPathResource("movies.csv").inputStream.bufferedReader(Charsets.UTF_8)).map {
            Film.fromCsvRecord(it)
        }.associateBy { it.id }

    fun getFilms() = films.values
    fun getFilm(id:String) = films[id]
}