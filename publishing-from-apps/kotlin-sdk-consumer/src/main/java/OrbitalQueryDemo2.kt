import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.orbital.client.converter.run
import com.orbital.client.stream
import com.orbital.client.transport.okhttp.httpStream
import film.types.Title
import films.FilmId
import films.StreamingProviderName
import films.StreamingProviderPrice
import films.reviews.FilmReviewScore
import films.reviews.ReviewText
import reactor.kotlin.core.publisher.toFlux
import java.time.Duration

val mapper = jacksonObjectMapper().writerWithDefaultPrettyPrinter()















data class MyFilmData(
    val id: FilmId,
    val title: Title,

    val whereCanIWatchThis: StreamingProviderName,
    val cost: StreamingProviderPrice,

    val reviewScore: FilmReviewScore,
    val reviewText: ReviewText
    )

fun main() {
    val response = stream<NewFilmReleaseAnnouncement>()
        .asStreamOf<MyFilmData>()
        .run(httpStream("http://localhost:9022"))
        .toFlux()

        .doOnEach { signal ->
            // Log out the response
            println(mapper.writeValueAsString(signal.get()))
        }

        .blockLast(Duration.ofMinutes(10))
}
