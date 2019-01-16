package com.ledahl.apps.recieppyapi.config

import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy

@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
class WebSecurityConfiguration: WebSecurityConfigurerAdapter() {

    @Throws(Exception::class)
    override fun configure(http: HttpSecurity?) {
        http?.sessionManagement()
                ?.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                ?.and()
                ?.csrf()?.disable()
                ?.authorizeRequests()
                ?.antMatchers("/graphql")?.permitAll()
                ?.anyRequest()?.authenticated()
    }
}