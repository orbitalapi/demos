package com.petflix.oauth.films.resource.server

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.OAuthFlow;
import io.swagger.v3.oas.annotations.security.OAuthFlows;
import io.swagger.v3.oas.annotations.security.OAuthScope;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;

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