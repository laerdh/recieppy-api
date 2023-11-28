package com.ledahl.apps.recieppyapi.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.SecurityFilterChain
import org.springframework.web.filter.CommonsRequestLoggingFilter

@Configuration
class ResourceServerConfiguration {
    @Bean
    fun filterChain(http: HttpSecurity?): SecurityFilterChain? {
        http?.cors(Customizer.withDefaults())
                ?.authorizeHttpRequests { c -> c.requestMatchers("/graphql").authenticated() }
                ?.oauth2ResourceServer { c -> c.jwt(Customizer.withDefaults()) }

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