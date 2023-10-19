# Combining JSON and XML

This demo showcases services publishing a mix of XML and JSON, how to combine them, and expose the
composed services as a REST API.

This demo has a video walkthrough, talking about how it's built:

![Video walktrhough](https://cdn.loom.com/sessions/thumbnails/d7819e1108e7401094dbdad39796bbf4-1697719617654-with-play.gif)

## Result
Here's the final Taxi file that describes the services:

```taxi
import com.orbitalhq.formats.Xml
@Xml
model FilmList {
    item : Film[]
}

model Film {
    id : FilmId inherits Int
    title : FilmTitle inherits String
    yearReleased : YearReleased inherits Int
}

model Award {
    title : AwardTitle inherits String
    yearWon : YearWon inherits Int
}

@Xml
model ActorList {
    item : Actor[]
}

model Actor {
    id : ActorId inherits Int
    name : ActorName inherits String
}

service FilmsService {
    @HttpOperation(url = "http://localhost:8044/films", method = "GET")
    operation getAllFilms():FilmList

    @HttpOperation(url = "http://localhost:8044/film/{filmId}/cast", method = "GET")
    operation fetchCastForFilm(@PathVariable("filmId") filmId : FilmId):ActorList

    @HttpOperation(url = "http://localhost:8044/film/{filmId}/awards", method = "GET")
    operation fetchAwardsForFilm(@PathVariable("filmId") filmId : FilmId):Award[]
}
```

And here's the final query we executed:

```taxi
@HttpOperation(url = '/api/q/filmsAndAwards', method = 'GET')
query filmsAndAwards {
   find { FilmList } as (Film[]) -> {
       filmId : FilmId
       nameOfFilm : FilmTitle
       awards : Award[]
           cast : Actor[]
   }
}
```
