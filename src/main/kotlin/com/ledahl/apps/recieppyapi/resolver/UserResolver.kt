package com.ledahl.apps.recieppyapi.resolver

import com.coxautodev.graphql.tools.GraphQLResolver
import com.ledahl.apps.recieppyapi.auth.model.AuthResponse
import com.ledahl.apps.recieppyapi.model.User

class UserResolver: GraphQLResolver<User> {
    fun getUser(authResponse: AuthResponse): User {
        return authResponse.user
    }
}