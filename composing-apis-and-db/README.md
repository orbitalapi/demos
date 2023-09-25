# Composing APIs and DBs - Spring Boot Edition

This demo focused on stitching together APIs and a Database call, to create a custom API for our needs.  You'd typically use this in a Backend-for-frontend (BFF) pattern.

This demo takes place in a fictitious film studio called Petflix.

## running
```bash
docker compose up -d
```

## Design choices
 * The services are written in Kotlin, and described using Taxi
   * Other alternatives are describing APIs using [OpenAPI with Taxi Metadata](https://orbitalhq.com/docs/describing-data-sources/open-api)
   * Generating API specs directly from our code
 * The database is a postgres db, using the [Pagila](https://github.com/devrimgunduz/pagila) sample schema
 * We've built everything in a single Spring Boot app, to reduce the noise in the demo code
   * In practice, this would be multiple separate microservices 



## Building & Pushing.
  1. Generate a Github access token that has container regsitry access
 
  2. Login

```bash
echo $GITHUB_ACCESS_TOKEN | docker login ghcr.io -u USERNAME --password-stdin
```


3. Build

```bash
mvn install
docker build . -t ghcr.io/orbitalapi/demos-composing-apis --load    
docker push ghcr.io/orbitalapi/demos-composing-apis
```
