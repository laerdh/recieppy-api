package com.ledahl.apps.recieppyapi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity

@SpringBootApplication
@EnableAsync
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
class RecieppyApiApplication {}

fun main(args: Array<String>) {
	runApplication<RecieppyApiApplication>(*args)
}