package com.ledahl.apps.recieppyapi.config

import com.ledahl.apps.recieppyapi.auth.JwtAuthenticationManagerResolver
import com.ledahl.apps.recieppyapi.config.properties.JwtProperties
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.SecurityFilterChain
import org.springframework.web.filter.CommonsRequestLoggingFilter

@Configuration
class ResourceServerConfiguration(@Autowired private val jwtProperties: JwtProperties) {

    @Bean
    fun filterChain(http: HttpSecurity?): SecurityFilterChain? {
        val authenticationManagerResolver = JwtAuthenticationManagerResolver(jwtProperties.issuers)

        http?.cors(Customizer.withDefaults())
                ?.authorizeHttpRequests { c -> c.requestMatchers("/graphql").authenticated() }
                ?.oauth2ResourceServer { c ->
                    c.authenticationManagerResolver(authenticationManagerResolver)
                }

        return http?.build()
    }

    @Bean
    fun requestLoggingFilter(): CommonsRequestLoggingFilter {
        val loggingFilter = CommonsRequestLoggingFilter()
        loggingFilter.setIncludeClientInfo(true)
        loggingFilter.setIncludeHeaders(true)
        loggingFilter.setIncludePayload(true)
        loggingFilter.setIncludeQueryString(true)
        return loggingFilter
    }
}