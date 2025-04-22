package com.petflix.oauth.films.data

import org.springdoc.core.customizers.GlobalOpenApiCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class OpenApiConfiguration {
    @Bean
    open fun openApiCustomiser(): GlobalOpenApiCustomizer {
        return GlobalOpenApiCustomizer { openApi ->
            openApi.components?.schemas?.get("Cast")?.properties?.get("id")?.let { prop ->
                val extensions = prop.extensions ?: mutableMapOf()
                extensions["x-taxi-type"] = mapOf(
                    "name" to "petflix.CastId",
                    "create" to true) // actual boolean
                prop.extensions = extensions
            }

            openApi.paths?.values?.forEach { path ->
                path.readOperations()?.forEach { operation ->
                    when(operation.operationId) {
                        "getWriter" ->  {
                            val parameter =  operation.parameters.first()
                            val extensions = parameter.schema.extensions ?: mutableMapOf()
                            extensions["x-taxi-type"] = mapOf("name" to "petflix.WriterId", "create" to true)
                            parameter.schema.extensions = extensions
                        }
                        "getDirector" -> {
                            val parameter =  operation.parameters.first()
                            val extensions = parameter.schema.extensions ?: mutableMapOf()
                            extensions["x-taxi-type"] = mapOf("name" to "petflix.DirectorId", "create" to true)
                            parameter.schema.extensions = extensions
                        }

                        "getCast" -> {
                            val parameter =  operation.parameters.first()
                            val extensions = parameter.schema.extensions ?: mutableMapOf()
                            extensions["x-taxi-type"] = mapOf("name" to "petflix.CastId", "create" to true)
                            parameter.schema.extensions = extensions
                        }

                        else -> throw IllegalArgumentException("Unknown operation id '${operation.operationId}'")
                    }
                }
            }

            openApi.components?.schemas?.get("Cast")?.properties?.get("name")?.let { prop ->
                val extensions = prop.extensions ?: mutableMapOf()
                extensions["x-taxi-type"] = mapOf(
                    "name" to "petflix.CastName",
                    "create" to true) // actual boolean
                prop.extensions = extensions
            }

            openApi.components?.schemas?.get("Director")?.properties?.get("id")?.let { prop ->
                val extensions = prop.extensions ?: mutableMapOf()
                extensions["x-taxi-type"] = mapOf(
                    "name" to "petflix.DirectorId",
                    "create" to true) // actual boolean
                prop.extensions = extensions
            }

            openApi.components?.schemas?.get("Director")?.properties?.get("name")?.let { prop ->
                val extensions = prop.extensions ?: mutableMapOf()
                extensions["x-taxi-type"] = mapOf(
                    "name" to "petflix.DirectorName",
                    "create" to true) // actual boolean
                prop.extensions = extensions
            }

            openApi.components?.schemas?.get("Writer")?.properties?.get("id")?.let { prop ->
                val extensions = prop.extensions ?: mutableMapOf()
                extensions["x-taxi-type"] = mapOf(
                    "name" to "petflix.WriterId",
                    "create" to true) // actual boolean
                prop.extensions = extensions
            }

            openApi.components?.schemas?.get("Writer")?.properties?.get("name")?.let { prop ->
                val extensions = prop.extensions ?: mutableMapOf()
                extensions["x-taxi-type"] = mapOf(
                    "name" to "petflix.WriterName",
                    "create" to true) // actual boolean
                prop.extensions = extensions
            }
        }
    }
}