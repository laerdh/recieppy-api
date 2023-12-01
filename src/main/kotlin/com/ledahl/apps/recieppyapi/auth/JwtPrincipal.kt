package com.ledahl.apps.recieppyapi.auth

import com.ledahl.apps.recieppyapi.model.User
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.AbstractOAuth2TokenAuthenticationToken

class JwtPrincipal(private val jwt: Jwt, val user: User) : AbstractOAuth2TokenAuthenticationToken<Jwt>(jwt) {

    override fun getTokenAttributes(): MutableMap<String, Any> {
        return this.token.claims
    }

    override fun getName(): String {
        return this.jwt.subject
    }
}