package com.ledahl.apps.recieppyapi.auth

import com.ledahl.apps.recieppyapi.service.AuthService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.convert.converter.Converter
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtClaimNames
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter
import org.springframework.stereotype.Component

@Component
class JwtAuthenticationPrincipalConverter(@Autowired private val authService: AuthService) : Converter<Jwt, AbstractAuthenticationToken> {
    private val principalClaimName = JwtClaimNames.SUB
    private var jwtGrantedAuthoritiesConverter: Converter<Jwt, Collection<GrantedAuthority>> = JwtGrantedAuthoritiesConverter()
    override fun convert(source: Jwt): AbstractAuthenticationToken? {
        val jwtUser = authService.getOrCreateUserFromJwt(source)
        jwtUser.isAuthenticated = true
        return jwtUser
    }
}