package com.ledahl.apps.recieppyapi.auth.context

import com.ledahl.apps.recieppyapi.service.UserService
import graphql.servlet.DefaultGraphQLContextBuilder
import graphql.servlet.GraphQLContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import javax.servlet.http.HttpServletRequest

@Component
class ContextBuilder(@Autowired private val userService: UserService): DefaultGraphQLContextBuilder() {
    override fun build(httpServletRequest: HttpServletRequest?): GraphQLContext {
        val authentication = SecurityContextHolder.getContext().authentication
        val user = userService.handleUserAuthentication(authentication)

        return AuthContext(request = httpServletRequest, user = user)
    }
}