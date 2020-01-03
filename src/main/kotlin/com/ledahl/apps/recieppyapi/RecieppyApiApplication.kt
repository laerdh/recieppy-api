package com.ledahl.apps.recieppyapi

import com.coxautodev.graphql.tools.ObjectMapperConfigurer
import com.coxautodev.graphql.tools.ObjectMapperConfigurerContext
import com.coxautodev.graphql.tools.SchemaParserOptions
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import graphql.scalars.ExtendedScalars
import graphql.schema.GraphQLScalarType
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean


@SpringBootApplication
class RecieppyApiApplication {
	@Bean
	fun date(): GraphQLScalarType {
		return ExtendedScalars.Date
	}

	@Bean
	fun schemaParserOptions(): SchemaParserOptions? {
		return SchemaParserOptions
				.newOptions()
				.objectMapperConfigurer(ObjectMapperConfigurer { mapper: ObjectMapper, context: ObjectMapperConfigurerContext? ->
					mapper.registerModule(JavaTimeModule())
				}).build()
	}
}

fun main(args: Array<String>) {
	runApplication<RecieppyApiApplication>(*args)
}