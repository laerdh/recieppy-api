package com.ledahl.apps.recieppyapi.auth

import com.ledahl.apps.recieppyapi.exception.NotAuthenticatedException
import com.ledahl.apps.recieppyapi.model.User
import com.ledahl.apps.recieppyapi.repository.UserRepository
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.*
import javax.crypto.spec.SecretKeySpec

@Service
class TokenService(@Autowired private val userRepository: UserRepository) {
    private val logger = LoggerFactory.getLogger(TokenService::class.java)

    @Value("\${JWT_SECRET}")
    private lateinit var secret: String

    private val issuer = "recieppy-api-server"
    private val signingKey by lazy {
        val decodedKey = Base64.getDecoder().decode(secret)
        SecretKeySpec(decodedKey, 0, decodedKey.size, "HmacSHA512")
    }

    fun generateToken(user: User): String {
        val token = Jwts.builder()
                .setSubject(user.phoneNumber)
                .setIssuer(issuer)
                .setIssuedAt(Date())
                .signWith(signingKey, SignatureAlgorithm.HS512)
                .compact()

        logger.info("Generated token for user: {}", user.phoneNumber)
        return token
    }

    @Throws(NotAuthenticatedException::class)
    fun verifyToken(token: String) {
        userRepository.getUserFromToken(token) ?: throw NotAuthenticatedException("Token invalid")
        try {
            val claims = Jwts.parser()
                    .setSigningKey(signingKey)
                    .parseClaimsJws(token)
                    .body

            val phoneNumber = claims.subject
            logger.info("Verified token for user: {}", phoneNumber)
        } catch (exception: JwtException) {
            throw NotAuthenticatedException("Token invalid")
        }
    }
}