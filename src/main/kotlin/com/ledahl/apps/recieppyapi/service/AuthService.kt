package com.ledahl.apps.recieppyapi.service

import com.google.firebase.auth.FirebaseAuth
import com.ledahl.apps.recieppyapi.auth.model.AuthData
import com.ledahl.apps.recieppyapi.auth.model.AuthResponse
import com.ledahl.apps.recieppyapi.exception.NotAuthenticatedException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class AuthService(@Autowired private val firebaseAuth: FirebaseAuth,
                  @Autowired private val tokenService: TokenService,
                  @Autowired private val userService: UserService) {
    private val logger = LoggerFactory.getLogger(AuthService::class.java)

    @Throws(NotAuthenticatedException::class)
    fun authenticate(authData: AuthData): AuthResponse {
        if (authData.phoneNumber.isNullOrEmpty() || authData.uid.isNullOrEmpty()) {
            throw NotAuthenticatedException("Credentials must be provided")
        }

        try {
            val firebaseUser = firebaseAuth.getUser(authData.uid)
            if (firebaseUser.uid == null
                    || firebaseUser.phoneNumber == null
                    || authData.phoneNumber != firebaseUser.phoneNumber) {
                throw NotAuthenticatedException("Phone number not found")
            }

            val existingUser = userService.getUserFromPhoneNumber(authData.phoneNumber)
            val generatedToken = tokenService.generateToken(authData.phoneNumber)

            if (existingUser == null) {
                val newAuthenticatedUser = userService.createUser(phoneNumber = authData.phoneNumber, token = generatedToken)
                logger.info("Created user with phone number: {}", authData.phoneNumber)
                return AuthResponse(user = newAuthenticatedUser.copy(token = generatedToken), firstLogin = true)
            }

            val authenticatedUser = existingUser.copy(token = generatedToken)
            userService.saveToken(authenticatedUser)
            return AuthResponse(user = authenticatedUser)
        } catch (exception: Exception) {
            logger.info("Failed to authenticate user with phone number: {} and uid: {}", authData.phoneNumber, authData.uid)
            throw NotAuthenticatedException("Authentication failed")
        }
    }
}