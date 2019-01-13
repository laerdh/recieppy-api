package com.ledahl.apps.recieppyapi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.EnableAspectJAutoProxy

@SpringBootApplication
@EnableAspectJAutoProxy
class RecieppyApiApplication

fun main(args: Array<String>) {
	runApplication<RecieppyApiApplication>(*args)
}