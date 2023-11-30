package com.ledahl.apps.recieppyapi.auth

import com.ledahl.apps.recieppyapi.config.properties.JwtIssuerProperties
import jakarta.servlet.http.HttpServletRequest
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.AuthenticationManagerResolver
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtDecoders
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider
import org.springframework.security.oauth2.server.resource.authentication.JwtIssuerAuthenticationManagerResolver
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class JwtAuthenticationManagerResolver(private val jwtAuthenticationUserConverter: JwtAuthenticationPrincipalConverter,
                                       private val trustedIssuers: List<JwtIssuerProperties>) : AuthenticationManagerResolver<HttpServletRequest> {

    override fun resolve(request: HttpServletRequest?): AuthenticationManager {
        val authenticationManagerResolver = JwtIssuerAuthenticationManagerResolver(object : AuthenticationManagerResolver<String> {
            fun getJwtSecretDecoder(jwtSecret: String): JwtDecoder {
                val sha256Hmac = Mac.getInstance("HmacSHA256")
                val key = jwtSecret.toByteArray()
                val secretKey = SecretKeySpec(key, 0, key.size, sha256Hmac.algorithm)
                sha256Hmac.init(secretKey)

                return NimbusJwtDecoder.withSecretKey(secretKey)
                    .build()
            }

            override fun resolve(issuer: String): AuthenticationManager {
                val issuerProperties = trustedIssuers.first { it.issuerUri == issuer }
                val jwkSetUri = issuerProperties.jwkSetUri

                val hasIssuerUri = jwkSetUri.startsWith("http://") || jwkSetUri.startsWith("https://")
                val jwtDecoder = if (hasIssuerUri) JwtDecoders.fromIssuerLocation(issuer) else getJwtSecretDecoder(jwkSetUri)

                return AuthenticationManager { authentication ->
                    val authenticationProvider = JwtAuthenticationProvider(jwtDecoder)
                    authenticationProvider.setJwtAuthenticationConverter(jwtAuthenticationUserConverter)
                    authenticationProvider.authenticate(authentication)
                }
            }
        })

        return authenticationManagerResolver.resolve(request)
    }
}