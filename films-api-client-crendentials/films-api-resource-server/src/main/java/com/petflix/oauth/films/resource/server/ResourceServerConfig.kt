package com.petflix.oauth.films.resource.server

import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtDecoders
import org.springframework.security.web.SecurityFilterChain


@EnableWebSecurity
@Configuration(proxyBeanMethods = false)
open class ResourceServerConfig {
    @Bean
    open fun securityFilterChain(http: HttpSecurity): SecurityFilterChain? {
        http
            .securityMatcher("/api/**")
            .authorizeHttpRequests(AuthoriseHttpRequestCustomizer())
            .oauth2ResourceServer(Customizer { oauth2ResourceServer: OAuth2ResourceServerConfigurer<HttpSecurity?>? ->
                oauth2ResourceServer!!.jwt(
                    Customizer.withDefaults()
                )
            }
            )
        return http.build()
    }


}

class AuthoriseHttpRequestCustomizer :
    Customizer<AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry> {
    override fun customize(authorize: AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry) {
        authorize.requestMatchers(
            "/api/**"
        ).authenticated()
    }

}