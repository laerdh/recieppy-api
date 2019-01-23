package com.ledahl.apps.recieppyapi.auth.context

import com.ledahl.apps.recieppyapi.service.TokenService
import com.ledahl.apps.recieppyapi.model.User
import com.ledahl.apps.recieppyapi.repository.UserRepository
import graphql.servlet.DefaultGraphQLContextBuilder
import graphql.servlet.GraphQLContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import javax.servlet.http.HttpServletRequest

@Component
class ContextBuilder(@Autowired private val userRepository: UserRepository,
                     @Autowired private val tokenService: TokenService): DefaultGraphQLContextBuilder() {
    override fun build(httpServletRequest: HttpServletRequest?): GraphQLContext {
        val user: User? = httpServletRequest
                ?.getHeader("Authorization")
                ?.takeIf { it.isNotEmpty() }
                ?.let {
                    val user = userRepository.getUserFromToken(it)
                    tokenService.verifyUserToken(user)
                }

        return AuthContext(request = httpServletRequest, user = user)
    }
}