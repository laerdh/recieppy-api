package com.ledahl.apps.recieppyapi.service

import com.ledahl.apps.recieppyapi.exception.NotAuthenticatedException
import com.ledahl.apps.recieppyapi.model.User
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.*
import javax.crypto.spec.SecretKeySpec

@Service
class TokenService {
    private val logger = LoggerFactory.getLogger(TokenService::class.java)

    @Value("\${JWT_SECRET}")
    private lateinit var secret: String

    private val issuer = "recieppy-api-server"
    private val signingKey by lazy {
        val decodedKey = Base64.getDecoder().decode(secret)
        SecretKeySpec(decodedKey, 0, decodedKey.size, "HmacSHA512")
    }

    fun generateToken(phoneNumber: String): String {
        val token = Jwts.builder()
                .setSubject(phoneNumber)
                .setIssuer(issuer)
                .setIssuedAt(Date())
                .signWith(signingKey, SignatureAlgorithm.HS512)
                .compact()

        logger.info("Generated token for user: {}", phoneNumber)
        return token
    }

    fun verifyUserToken(user: User?): User? {
        try {
            val claims = Jwts.parser()
                    .setSigningKey(signingKey)
                    .parseClaimsJws(user?.token)
                    .body

            val phoneNumber = claims.subject
            logger.info("Verified token for user: {}", phoneNumber)
        } catch (exception: Exception) {
            logger.info("Failed to verify token for user: {}", user?.phoneNumber)
            return null
        }
        return user
    }
}