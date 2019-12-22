package com.ledahl.apps.recieppyapi.config

import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter

@EnableWebSecurity
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
}