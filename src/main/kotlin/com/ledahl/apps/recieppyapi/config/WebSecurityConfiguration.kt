package com.ledahl.apps.recieppyapi.config

import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter

@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
class WebSecurityConfiguration(): WebSecurityConfigurerAdapter() {

    override fun configure(auth: AuthenticationManagerBuilder?) {
        auth?.inMemoryAuthentication()?.withUser("user")?.password("password")?.roles("USER")
    }

    @Throws(Exception::class)
    override fun configure(http: HttpSecurity?) {
        http?.csrf()?.disable()
                ?.authorizeRequests()
                ?.antMatchers("/graphql")?.permitAll()
                ?.anyRequest()?.authenticated()
                ?.and()
                ?.formLogin()
    }
}