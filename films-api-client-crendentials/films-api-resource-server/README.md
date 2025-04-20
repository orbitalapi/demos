# Films API

This is a simple Spring boot application to expose two apis:

## `/api/films`

Returns IMDB Top 100 Films

## `/api/films/{id}`

Returns the IMDB Top 100 film for the given film imdb id.

## Swagger UI

[Swagger UI For the API](http://localhost:8080/swagger-ui/index.html)

## Security

All end points under `/api` is protected by OAuth 2.0 Bearer Tokens. The application has delegated its authority management to an authorisation server running on localhost 9000. Here is the corresponding
configuration from [application.yml](./src/main/resources/application.yml)

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          jwk-set-uri: http://localhost:9000/oauth2/jwks
```

## Api Data

see [Movies data Csv file.](./src/main/resources/movies.csv)