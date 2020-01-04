package com.ledahl.apps.recieppyapi.config

import org.springframework.context.annotation.Bean
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.web.filter.CommonsRequestLoggingFilter

@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
class ResourceServerConfiguration: WebSecurityConfigurerAdapter() {

    @Throws(Exception::class)
    override fun configure(http: HttpSecurity?) {
        http?.cors()?.and()
                ?.authorizeRequests()
                ?.antMatchers("/graphql")?.authenticated()
                ?.anyRequest()?.denyAll()
                ?.and()
                ?.oauth2ResourceServer()
                ?.jwt()
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