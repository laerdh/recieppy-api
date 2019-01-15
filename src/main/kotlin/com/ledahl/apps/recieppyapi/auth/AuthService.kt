package com.ledahl.apps.recieppyapi.auth

import com.google.firebase.auth.FirebaseAuth
import com.ledahl.apps.recieppyapi.auth.model.AuthData
import com.ledahl.apps.recieppyapi.auth.model.AuthResponse
import com.ledahl.apps.recieppyapi.exception.NotAuthenticatedException
import com.ledahl.apps.recieppyapi.exception.UserNotFoundException
import com.ledahl.apps.recieppyapi.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class AuthService(@Autowired private val firebaseAuth: FirebaseAuth,
                  @Autowired private val tokenService: TokenService,
                  @Autowired private val userRepository: UserRepository) {
    private val logger = LoggerFactory.getLogger(AuthService::class.java)

    @Throws(NotAuthenticatedException::class)
    fun authenticate(authData: AuthData): AuthResponse {
        if (authData.phoneNumber.isNullOrEmpty() || authData.uid.isNullOrEmpty()) {
            throw NotAuthenticatedException("Credentials must be provided")
        }

        try {
            val firebaseUser = firebaseAuth.getUser(authData.uid)
            if (firebaseUser.uid == null || firebaseUser.phoneNumber == null
                    && authData.phoneNumber != firebaseUser.phoneNumber) {
                throw NotAuthenticatedException("Bad credentials")
            }

            val existingUser = userRepository.getUserFromPhoneNumber(firebaseUser.phoneNumber) ?: throw UserNotFoundException()
            val generatedToken = tokenService.generateToken(existingUser)
            val authenticatedUser = existingUser.copy(token = generatedToken)
            userRepository.saveTokenForUser(authenticatedUser)

            return AuthResponse(user = authenticatedUser, token = generatedToken)
        } catch (exception: Exception) {
            logger.info("Failed to authenticate user with phone number: {} and uid: {}", authData.phoneNumber, authData.uid)
            throw NotAuthenticatedException("Bad credentials")
        }
    }
}