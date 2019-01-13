package com.ledahl.apps.recieppyapi.auth

import com.google.firebase.auth.FirebaseAuth
import com.ledahl.apps.recieppyapi.auth.model.AuthData
import com.ledahl.apps.recieppyapi.auth.model.AuthResponse
import com.ledahl.apps.recieppyapi.exception.NotAuthenticatedException
import com.ledahl.apps.recieppyapi.repository.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class AuthService(@Autowired private val firebaseAuth: FirebaseAuth,
                  @Autowired private val userRepository: UserRepository) {

    @Throws(NotAuthenticatedException::class)
    fun authenticate(authData: AuthData): AuthResponse {
        if (authData.phoneNumber.isNullOrEmpty() || authData.uid.isNullOrEmpty()) {
            throw NotAuthenticatedException("Authentication details missing")
        }

        try {
            val firebaseUser = firebaseAuth.getUser(authData.uid)
            if (firebaseUser.uid == null || firebaseUser.phoneNumber == null) {
                throw NotAuthenticatedException("Bad credentials")
            }

            /**
             * TODO:
             * 1. Create user if not found?
             * 2. Generate new JWT and store it in db
             */
            val user = userRepository.getUserFromPhoneNumber(firebaseUser.phoneNumber)
            return AuthResponse(
                    name = user?.name ?: "",
                    token = user?.token
            )
        } catch (exception: Exception) {
            // TODO: Add log statement
            throw NotAuthenticatedException("Bad credentials")
        }
    }
}