package com.petflix.v2.reviews

import films.reviews.FilmReviewScore
import films.reviews.ReviewText
import films.reviews.SquashedTomatoesFilmId
import lang.taxi.annotations.Operation
import lang.taxi.annotations.Service
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import java.math.RoundingMode
import kotlin.random.Random

@Service
@RestController
class ReviewsService {

    private val reviewText = listOf(
        "An aggressively fine and mostly enjoyable romp that does some things well and others things less so. It’s the epitome of just OK.",
        "For a while it seems it wants to be the franchise’s ‘Mission: Impossible.’ Instead, it’s the anti–‘Top Gun: Maverick’.My co-worker Ali has one of these. He says it looks towering.",
        "An ugly franchise entry that feels like a contractual obligation by an employee full of hatred. Easily the worst film in the JP universe.",
        "This is not one of those awful dark, depressing films about an impending genetic apocalypse, although it could have easily been turned into that with a few minor tweaks. This is an entertaining romp, loaded with action, nostalgia and special effects.",
        "A death-metal ode to honor, retribution and sacrifice that casts payback in a surprisingly, and thrillingly, positive light.",
        "A new oddity in which horror, revenge and Norse mythology go hand in hand in one of the most suggestive and visceral shows that we've been able to enjoy on the big screen in the last years.",
        "Gratuitously extenuating retina trips... A nightmare in its own right that portrays the tragedy and the end of a treacherous and ultra-masculinized world. [Full review in Spanish]",
        "The veteran actor does nuanced work as usual, but the FX series takes too many ridiculous turns.",
        "The Old Man is more captivating when dealing with universal real-world concerns than with its spy stuff, no matter that its slam-bang action is crisp and its twists and turns are energized.",
        "Much of the show’s success comes down to Bridges, who anchors a rickety character visibly battered by the past yet able to shapeshift in the present.",
    )

    @GetMapping("/reviews/{filmId}")
    @Operation
    fun getReview(@PathVariable("filmId") filmId: SquashedTomatoesFilmId): FilmReview {
        return FilmReview(
            filmId,
            Random.nextDouble(2.3, 5.0).toBigDecimal().setScale(1, RoundingMode.HALF_EVEN),
            reviewText.random()
        )
    }
}

data class FilmReview(
    val filmId: SquashedTomatoesFilmId,
//    val filmId: FilmId,
    val score: FilmReviewScore,
    val filmReview: ReviewText
)
