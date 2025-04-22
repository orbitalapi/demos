package com.petflix.oauth.films.resource.server

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.OAuthFlow;
import io.swagger.v3.oas.annotations.security.OAuthFlows;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springdoc.core.customizers.GlobalOpenApiCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@OpenAPIDefinition(
    servers = [Server(url = "http://localhost:8080")], info = Info(
        title = "Film Service APIs",
        description = "This lists all Films Service API Calls. The Calls are OAuth2 secured, "
                + "so please use your client ID and Secret to test them out.",
        version = "v1.0"
    )
)
@SecurityScheme(
    name = "security_auth",
    type = SecuritySchemeType.OAUTH2,
    flows = OAuthFlows(
        clientCredentials = OAuthFlow(
            tokenUrl = "\${petflix.authroisationServer.tokenUrl:http://localhost:9000/oauth2/token}"
        )
    )
)
class OpenAPI3Configuration

@Configuration
open class OpenAPIConfig {
    @Bean
    open fun openApiCustomiser(): GlobalOpenApiCustomizer {
        return GlobalOpenApiCustomizer { openApi ->
            openApi.paths?.values?.forEach { path ->
                path.readOperations()?.forEach { operation ->
                    when(operation.operationId) {
                        "getFilm" ->  {
                            val parameter =  operation.parameters.first()

                            val extensions = parameter.schema.extensions ?: mutableMapOf()
                            extensions["x-taxi-type"] = mapOf("name" to "petflix.FilmId", "create" to true)
                            parameter.schema.extensions = extensions
                        }
                        else -> {
                            }
                        }
                    }
                }

            openApi.components?.schemas?.get("Film")?.properties?.get("id")?.let { prop ->
                val extensions = prop.extensions ?: mutableMapOf()
                extensions["x-taxi-type"] = mapOf(
                    "name" to "petflix.FilmId",
                    "create" to true) // actual boolean
                prop.extensions = extensions
            }

            openApi.components?.schemas?.get("Film")?.properties?.get("title")?.let { prop ->
                val extensions = prop.extensions ?: mutableMapOf()
                extensions["x-taxi-type"] = mapOf(
                    "name" to "petflix.Title",
                    "create" to true) // actual boolean
                prop.extensions = extensions
            }

            openApi.components?.schemas?.get("Film")?.properties?.get("imdbRanking")?.let { prop ->
                val extensions = prop.extensions ?: mutableMapOf()
                extensions["x-taxi-type"] = mapOf(
                    "name" to "petflix.ImdbRanking",
                    "create" to true) // actual boolean
                prop.extensions = extensions
            }

            openApi.components?.schemas?.get("Film")?.properties?.get("year")?.let { prop ->
                val extensions = prop.extensions ?: mutableMapOf()
                extensions["x-taxi-type"] = mapOf(
                    "name" to "petflix.ReleaseYear",
                    "create" to true) // actual boolean
                prop.extensions = extensions
            }

            openApi.components?.schemas?.get("Film")?.properties?.get("imdbVotes")?.let { prop ->
                val extensions = prop.extensions ?: mutableMapOf()
                extensions["x-taxi-type"] = mapOf(
                    "name" to "petflix.ImdbVotes",
                    "create" to true) // actual boolean
                prop.extensions = extensions
            }

            openApi.components?.schemas?.get("Film")?.properties?.get("imdbRating")?.let { prop ->
                val extensions = prop.extensions ?: mutableMapOf()
                extensions["x-taxi-type"] = mapOf(
                    "name" to "petflix.ImdbRating",
                    "create" to true) // actual boolean
                prop.extensions = extensions
            }

            openApi.components?.schemas?.get("Film")?.properties?.get("certificate")?.let { prop ->
                val extensions = prop.extensions ?: mutableMapOf()
                extensions["x-taxi-type"] = mapOf(
                    "name" to "petflix.Certificate",
                    "create" to true) // actual boolean
                prop.extensions = extensions
            }

            openApi.components?.schemas?.get("Film")?.properties?.get("duration")?.let { prop ->
                val extensions = prop.extensions ?: mutableMapOf()
                extensions["x-taxi-type"] = mapOf(
                    "name" to "petflix.Duration",
                    "create" to true) // actual boolean
                prop.extensions = extensions
            }

            openApi.components?.schemas?.get("Film")?.properties?.get("genres")?.let { prop ->
                prop.items?.let { item ->
                    val extensions = item.extensions ?: mutableMapOf()
                    extensions["x-taxi-type"] = mapOf(
                        "name" to "petflix.Genre",
                        "create" to true) // actual boolean
                    item.extensions = extensions
                }
            }

            openApi.components?.schemas?.get("Film")?.properties?.get("imageLink")?.let { prop ->
                val extensions = prop.extensions ?: mutableMapOf()
                extensions["x-taxi-type"] = mapOf(
                    "name" to "petflix.ImageLink",
                    "create" to true) // actual boolean
                prop.extensions = extensions
            }

            openApi.components?.schemas?.get("Film")?.properties?.get("cast")?.let { prop ->
                prop.items?.let { item ->
                    val extensions = item.extensions ?: mutableMapOf()
                    extensions["x-taxi-type"] = mapOf(
                        "name" to "petflix.CastId",
                        "create" to true) // actual boolean
                    item.extensions = extensions
                }
            }

            openApi.components?.schemas?.get("Film")?.properties?.get("directors")?.let { prop ->
                prop.items?.let { item ->
                    val extensions = item.extensions ?: mutableMapOf()
                    extensions["x-taxi-type"] = mapOf(
                        "name" to "petflix.DirectorId",
                        "create" to true) // actual boolean
                    item.extensions = extensions
                }
            }

            openApi.components?.schemas?.get("Film")?.properties?.get("writers")?.let { prop ->
                prop.items?.let { item ->
                    val extensions = item.extensions ?: mutableMapOf()
                    extensions["x-taxi-type"] = mapOf(
                        "name" to "petflix.WriterId",
                        "create" to true) // actual boolean
                    item.extensions = extensions
                }
            }
        }
    }
}