package com.orbital.films

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.orbital.client.converter.run
import com.orbital.client.find
import com.orbital.client.transport.okhttp.http
import demo.netflix.NetflixFilmId
import film.Film
import film.types.Title
import films.FilmId
import films.StreamingProviderName
import films.StreamingProviderPrice
import films.reviews.FilmReviewScore
import films.reviews.ReviewText
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldNotHaveSize
import lang.taxi.annotations.DataType
import lang.taxi.utils.log
import reactor.kotlin.core.publisher.toFlux

class FilmDemoTest : DescribeSpec({
    xdescribe("running against the films stack") {
        it("should run the query against the Films Demo stack") {
            data class Response(
                val id: FilmId,
                val title: Title,

                val streamingProviderName: StreamingProviderName,
                val cost: StreamingProviderPrice,

                val reviewScore: FilmReviewScore,
                val reviewText: ReviewText
            )


            val response = find<List<Film>>()
                .asA<List<Response>>()
                .run(http("http://localhost:9022"))
                .toFlux()
                .blockLast()!!
            response.shouldNotHaveSize(100)
        }

        it("should stream queries") {

            @DataType("NewFilmReleaseAnnouncement")
            data class NewFilmReleaseAnnouncement(
                val filmId: NetflixFilmId
            )

            data class Response(
                val id: FilmId,
                val title: Title,

                val streamingProviderName: StreamingProviderName,
                val cost: StreamingProviderPrice,

                val reviewScore: FilmReviewScore,
                val reviewText: ReviewText
            )

            com.orbital.client.stream<NewFilmReleaseAnnouncement>()
                .asA<Response>()
                .run(com.orbital.client.transport.okhttp.http("http://localhost:9022"))
                .toFlux()
                .subscribe { event ->
                    log().info("Received a message: ${jacksonObjectMapper().writer().writeValueAsString(event)}")
                }
//        Thread.sleep(1000000)


        }
    }


})