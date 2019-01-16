package com.ledahl.apps.recieppyapi.exception

import graphql.ExceptionWhileDataFetching
import graphql.GraphQLError
import graphql.servlet.GraphQLErrorHandler
import org.springframework.stereotype.Component

@Component
class CustomGraphQLErrorHandler: GraphQLErrorHandler {
    override fun processErrors(errors: MutableList<GraphQLError>?): MutableList<GraphQLError> {
        val clientErrors = errors
                ?.filter(this::isClientError)
        val serverErrors = errors
                ?.filter { e -> !isClientError(e) }
                ?.map { e -> GraphQLErrorAdapter(e) }

        val allErrors = mutableListOf<GraphQLError>()
        clientErrors?.let {
            allErrors.addAll(it)
        }
        serverErrors?.let {
            allErrors.addAll(it)
        }
        return allErrors
    }

    private fun isClientError(error: GraphQLError): Boolean {
        return !(error is ExceptionWhileDataFetching || error is Throwable)
    }
}