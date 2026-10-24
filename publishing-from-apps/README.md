# Publishing schemas from Spring Boot apps

> **Status (Orbital 0.39):** the services publish with `schema-publisher-spring-boot-starter` (0.39.1-M1),
> which needs Java 21 and Spring Boot 4; this module builds on its own, separately from the Spring Boot 2.7
> demos parent. Everything here runs natively on Apple Silicon: Orbital comes from the start script and the
> services run from your IDE or `java -jar`. Only the optional Pagila database (borrowed from
> `importing-through-ui`) is an amd64-only image. `kotlin-sdk-consumer` is excluded from the build until
> it's ported to the current JVM query SDK.

Companion code for the [Generating Taxi from code](https://orbitalhq.com/docs/guides/generating-taxi-from-code) guide.

Three small Spring Boot services, each of which generates Taxi from its `@RestController`s at startup and
publishes it to Orbital. There's no Taxi to hand-write for the APIs: the starter introspects the controllers,
and Kotlin type aliases carry the semantic types.

| Module | Port | Publishes |
|--------|------|-----------|
| `film-listing-api` | 9981 | `/films/{filmId}/streamingProviders` - where a film can be watched. Also pushes `NewFilmReleaseAnnouncement` protobuf messages to a Kafka topic, if Kafka is running on `localhost:9092` |
| `squashed-tomatoes-ratings` | 9985 | `/reviews/{filmId}` - a review for a film, keyed by the review site's own id |
| `id-resolution-service` | 9986 | `/ids/{scheme}/{id}` - maps between our `FilmId`, the review site's `SquashedTomatoesFilmId` and Netflix's `NetflixFilmId` |
| `films-taxonomy` | - | The Kotlin type aliases (`FilmId`, `ReviewText`, ...) the services share. Each is `@DataType(..., imported = true)`, pointing at a type declared in the Taxi project below |
| `taxi/` | - | The Taxi project declaring those types, plus a `film` table from the Pagila database (optional) |

## How publishing works

Each service has the starter on its classpath:

```xml
<dependency>
   <groupId>com.orbitalhq</groupId>
   <artifactId>schema-publisher-spring-boot-starter</artifactId>
   <version>${orbital.version}</version>
</dependency>
```

and tells it where Orbital is and what to call the published project, in `application.yml`:

```yaml
orbital:
  schema:
    publisher:
      orbital-url: ws://localhost:9022       # ws:// re-publishes automatically if Orbital restarts
      base-url: http://localhost:9986        # where Orbital should call this service
      package-id: io.petflix.demos/id-lookup-service/1.0.0
```

That's the whole integration. The one piece of code is a `TypeAliasRegister.registerPackage(...)` call in each
`main`, telling the generator which packages hold the Kotlin type aliases - without it, a `FilmId` parameter
would be published as a plain `Int`.

## Running it

1. Start Orbital with the start script, from an empty directory:

   ```bash
   curl -fsSL https://start.orbitalhq.com | bash
   ```

   Sign in when it opens http://localhost:9022.

2. Give Orbital the shared types. The services *import* `films.FilmId` and friends, so the Taxi project that
   declares them needs to be in the workspace. Copy it into the start script's data directory and register it:

   ```bash
   mkdir -p <start-script-dir>/orbital_data/orgs/workspaces/default/default/orbital/workspace/projects/com.petflix
   cp -r taxi <start-script-dir>/orbital_data/orgs/workspaces/default/default/orbital/workspace/projects/com.petflix/films-demo
   ```

   then in Orbital go to **Projects**, **Add project**, choose **Local disk** and enter
   `orbital/workspace/projects/com.petflix/films-demo` as the path.

3. Build and start the services (Java 21):

   ```bash
   mvn package -DskipTests
   java -jar id-resolution-service/target/id-resolution-service.jar
   java -jar squashed-tomatoes-ratings/target/squashed-tomatoes-ratings.jar
   java -jar film-listing-api/target/film-listing-api.jar
   ```

   Each logs `Schema submitted successfully` once it has published, and shows up under **Projects** in
   Orbital as *Pushed to Orbital*. `film-listing-api` also wants a Kafka broker on `localhost:9092` for its
   announcements; without one it still publishes its schema, but logs a connection error every second.

4. Ask for data across all three, in Orbital's query editor:

   ```taxi
   given { id : FilmId = 1 }
   find {
      availableOn: StreamingProviderName
      price: StreamingProviderPrice
      review: FilmReviewScore
      reviewText: ReviewText
   }
   ```

   Orbital resolves `FilmId` to a `SquashedTomatoesFilmId` through the id service to fetch the review.

### Known limitations with Orbital `next` (0.39.1)

Checked against `orbitalhq/orbital:next` on 2026-10-24, with the demo's projects loaded into the default workspace:

 * **Sign-in applies to the publisher endpoint.** With the start script's default configuration, Orbital asks
   you to sign in, and `/api/ws/schemas` (like the rest of `/api/**`) then answers `401` unless the publisher
   sends a bearer token (`orbital.schema.publisher.auth.headers.Authorization`). Running Orbital with
   `--orbital.security.open-idp.enabled=false` lets the services publish without one.
 * **Pushed schemas are validated on their own, not against the workspace.** In `OrgAndWorkspace` mode the
   published package is compiled in a separate store, so `import films.FilmId` fails with
   *Cannot import films.FilmId as it is not defined* even though the Taxi project declaring it is in the
   workspace. The Taxi the starter generates is right (it references `films.FilmId`, not `Int`); it's the
   server side that doesn't yet see the workspace's types. Declaring the types in each service instead
   (`imported = false`) doesn't help, because the second service to publish then clashes with the first
   (*Symbol films.reviews.SquashedTomatoesFilmId is already declared*).

Both are platform-side, and are noted on ORB-1251.

### Optional: the films database

`taxi/src/film/Film.taxi` describes the `film` table of the Pagila sample database, with `film_id` typed as
the same `FilmId` the services use, so `find { Film[] } as { ... }` can enrich every film from the APIs.
`taxi/orbital/config/connections.conf` defines the `films` connection; its defaults (`env.conf`) point at the
Pagila container started by `../importing-through-ui/docker-compose.yml` (host port 35432).
