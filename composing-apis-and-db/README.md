# Composing APIs and DBs - Spring Boot Edition

> **Status (Orbital 0.39):** this demo follows the Orbital start-script layout (`./orbital_data`,
> `orbital.*` options). On Apple Silicon, Orbital and Postgres run natively; the demo services image
> (`ghcr.io/orbitalapi/demos-composing-apis`) is published for `linux/amd64` only, so Docker Desktop
> runs it under emulation (`platform: linux/amd64` in the compose file). It starts more slowly but
> works. To run it natively, build the image yourself - see [BUILDING.md](BUILDING.md).

This demo focuses on stitching together APIs and a Database call, to create a custom API for our needs.  You'd typically use this in a Backend-for-frontend (BFF) pattern.

This demo takes place in a fictitious film studio called Petflix, where we'll stitch together data about our Films catalog, along with data from REST APIs, like the reviews of each film, and where we can watch it.

It accompanies the [Composing an API and a database](https://orbitalhq.com/docs/guides/composing-api-and-database) guide.

## Our demo services
![architecture](architecture.png)

This demo deploys the following:

 * A database exposing a catalog of Films (the Postgres [Pagila](https://github.com/devrimgunduz/pagila) sample database)
 * A Spring Boot service, containing:
   * A REST Endpoint that returns which streaming services are playing each film
   * A REST Endpoint that returns reviews for each film
   * A REST Endpoint that resolves ids.
     * Our services have different ID schemes - specifically the IDs used in our DB are not the same IDs used by our FilmReviews API. Therefore, we need to map one set of Ids to another.
 * Orbital, which learns about the Spring Boot service from its OpenAPI spec, annotated with Taxi types (`x-taxi-type`).
   * Alternatives are publishing the schema directly from the app on startup (see the `publishing-from-apps` demo), or hand-writing Taxi.

## What the demo shows

 * ([Link](#composing-apis)) Building a TaxiQL for data sourced from the three different stores (Db, WatchingService, ReviewService)
 * Resolving the different ID schemes
 * ([Link](#exposing-a-composite-api)) Exposing a REST API that returns our composed result set

## Exploring the components

### The workspace (`orbital_data/`)
Orbital keeps everything it reads and writes under `./orbital_data`, exactly as the [start script](https://start.orbitalhq.com) lays it out:

```
orbital_data/orgs/organisations.conf                        the organisations and their workspaces
orbital_data/orgs/workspaces/default/default/workspace.conf  the default workspace: which projects it contains
orbital_data/orgs/workspaces/default/default/orbital/workspace/projects/
   com.petflix.demos/films/0.1.0/                            the Taxi project (below)
   com.petflix/PetflixServices/0.1.20/api-docs.yaml          the OpenAPI spec for the Spring Boot service
```

Projects you create from Orbital's UI are written alongside these. Everything else Orbital writes
there at runtime (license, search indexes, history queues) is git-ignored.

### Taxi project
A [Taxi](https://taxilang.org) project is at `orbital_data/orgs/workspaces/default/default/orbital/workspace/projects/com.petflix.demos/films/0.1.0`

 * `films.taxi` defines a handful of semantic types, that we use in our API specs. eg:

```taxi
type FilmId inherits Int

[[ The name of a streaming service where people can watch a movie ]]
type StreamingProviderName inherits String
```

 * `FilmDb.taxi` enriches the database schema, to add semantic types to the tables
 * `orbital/config/connections.conf` defines the `films` database connection that `FilmDb.taxi` refers to; the credentials live in `orbital/config/env.conf` next to it

### OpenAPI spec
The Spring Boot service's OpenAPI spec is at `orbital_data/orgs/workspaces/default/default/orbital/workspace/projects/com.petflix/PetflixServices/0.1.20/api-docs.yaml`.
It's registered in `workspace.conf` as an OpenAPI project, and annotated with `x-taxi-type` so each
parameter and response field maps onto a type from the Taxi project:

```yaml
parameters:
   -  name: filmId
      in: path
      schema:
         type: string
         x-taxi-type:
            name: films.reviews.SquashedTomatoesFilmId
```

### Auto generated Kotlin Code
This demo converts our Taxi types into Kotlin, so that we can use them directly
in our Service definitions.

For example:

```kotlin
    @GetMapping("/reviews/{filmId}")
    @Operation
    fun getReview(@PathVariable("filmId")
                  filmId: SquashedTomatoesFilmId // Note this is a semantic type, generated from our Taxi type defs
    ): FilmReview {
```

This step is optional, but means the code and the spec share one vocabulary.

Normally, you'd build these in a CI/CD step using `taxi build`, but we've checked them in to
make everything a little clearer: they're the Maven module at `taxonomy/`.

### Spring Boot Services
Spring boot services are at `services`.

There's a single app, with 3 rest APIs exposed. It's packaged as `ghcr.io/orbitalapi/demos-composing-apis`
(see [BUILDING.md](BUILDING.md)).


## Running this demo
Clone this repo, and cd into this directory

Run the following docker command to start Orbital, preconfigured with this project:

```bash
docker compose up -d
```

Orbital runs as your user, so the files it writes into `./orbital_data` stay editable. On Linux, if
your user isn't `1000:1000`, write your ids to a `.env` file first:

```bash
printf 'UID=%s\nGID=%s\n' "$(id -u)" "$(id -g)" > .env
```

The stack exposes Orbital on 9022, the demo services on 7033 and Postgres on 35432 (not 25432, so it
can sit next to a start-script stack). When you first open Orbital it asks you to sign in (or create
a free account) - that's how it fetches its license.

## Things to Try
After 20 seconds or so, open http://localhost:9022, and you should see Orbital's UI.

### Composing APIs
In the Code editor, write a query to ask for data coming from the 3 APIs.

```taxi
find { Film[] } as {
    id : FilmId
    title : Title

    review: FilmReviewScore
    reviewText: ReviewText

    availableOn: StreamingProviderName
    price: StreamingProviderPrice
}[]
```


Notice that as you're typing, you get nice code completion.

![code completion](./code-editing.gif)

Run this query, and you'll get the results back, linking together data from our Database, and 3 different REST APIs.


### Exploring the profiler
Click on the Profiler tab, and you'll see an architecture diagram, showing all
the services that were called for each field:

![profiler diagram](profiler-view.png)

Note that
 * To fetch our `serviceName` and `price`, we passed data from the Db to a REST API
 * To fetch the review data, we had to take a trip to an additional API to resolve the Ids


### How does this work?
There's no resolver or glue code written here, so how does this all work?

Orbital uses the types in our query (`FilmReviewScore`, `ReviewText`, etc), and looks up
the services that expose these values, then builds an integration plan to load the required
data.

## Exposing a composite API
Now we have the data we want to expose, we can publish this on an API.

 * First click "Save"
 * In the popup, for the project, select "films"
 * For the query name, enter `filmsAndReviews` (or any name you choose)
 * Click Save

![Save a query](save-query.png)

If you take a look in the source code, a new file has appeared at
`orbital_data/orgs/workspaces/default/default/orbital/workspace/projects/com.petflix.demos/films/0.1.0/src/filmsAndReviews.taxi`.

Next, let's expose this saved query as an HTTP endpoint.

 * In the top menu, click the 3-dots menu item
 * Click Publish query as HTTP Endpoint
 * In the popup, enter a URL for the query - eg: `films-and-reviews`
 * Click Update
 * Click Save

![Publishing a query as an HTTP endpoint](publish-as-http.gif)

Now, send a request to the endpoint you selected.  (As we're getting JSON back, we'll pipe it to `jq` so it's nicely formatted)

```bash
curl http://localhost:9022/api/q/films-and-reviews | jq
```

```json
  {
    "id": 904,
    "title": "TRAIN BUNCH",
    "review": 4.6,
    "reviewText": "This is not one of those awful dark, depressing films about an impending genetic apocalypse, although it could have easily been turned into that with a few minor tweaks. This is an entertaining romp, loaded with action, nostalgia and special effects.",
    "availableOn": "Netflix",
    "price": 9.99
  },
  {
    "id": 905,
    "title": "TRAINSPOTTING STRANGERS",
    "review": 3.9,
    "reviewText": "For a while it seems it wants to be the franchise’s ‘Mission: Impossible.’ Instead, it’s the anti–‘Top Gun: Maverick’.My co-worker Ali has one of these. He says it looks towering.",
    "availableOn": "Now TV",
    "price": 13.99
  },
```

![Querying from the CLI](cli-query.gif)

## Design choices
 * The services are written in Kotlin, and described to Orbital with [OpenAPI plus Taxi metadata](https://orbitalhq.com/docs/describing-data-sources/open-api)
   * Alternatives are publishing the schema from the app itself (the `publishing-from-apps` demo) or writing the Taxi by hand
 * The database is a postgres db, using the [Pagila](https://github.com/devrimgunduz/pagila) sample schema
 * We've built everything in a single Spring Boot app, to reduce the noise in the demo code
   * In practice, this would be multiple separate microservices
