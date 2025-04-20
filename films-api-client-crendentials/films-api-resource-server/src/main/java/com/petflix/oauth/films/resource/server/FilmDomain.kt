package com.petflix.oauth.films.resource.server

import org.apache.commons.csv.CSVRecord
import java.math.BigDecimal

data class Film(
    val id: String,
    val imdbRanking: Int,
    val title: String,
    val year: Int,
    val imdbVotes: Int,
    val imdbRating: BigDecimal,
    val certificate: String,
    val duration: Int,
    val genres: List<String>,
    val cast: List<String>,
    val directors: List<String>,
    val writers: List<String>,
    val imageLink: String
) {
    companion object {
        fun fromCsvRecord(record: CSVRecord): Film {
            return Film(
                id = record.get("id"),
                imdbRanking = record.get("rank").toInt(),
                title = record.get("name"),
                year = record.get("year").toInt(),
                imdbVotes = record.get("imdb_votes").toInt(),
                imdbRating = record.get("imdb_rating").toBigDecimal(),
                certificate = record.get("certificate"),
                duration = record.get("duration").toInt(),
                cast = record.get("cast_id").split(",").map { it.trim() },
                directors = record.get("director_id").split(",").map { it.trim() },
                writers = record.get("writer_id").split(",").map { it.trim() },
                genres = record.get("genre").split(",").map { it.trim() },
                imageLink = record.get("img_link")

            )
        }
    }
}



