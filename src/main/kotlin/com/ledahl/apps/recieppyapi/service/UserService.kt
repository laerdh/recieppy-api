package com.ledahl.apps.recieppyapi.service

import com.ledahl.apps.recieppyapi.exception.NotAuthorizedException
import com.ledahl.apps.recieppyapi.model.User
import com.ledahl.apps.recieppyapi.model.enums.UserRole
import com.ledahl.apps.recieppyapi.model.input.UserInput
import com.ledahl.apps.recieppyapi.repository.UserRepository
import graphql.GraphQLException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Service

@Service
class UserService(@Autowired private val userRepository: UserRepository) {
    fun getUsers(user: User?): List<User> {
        if (user?.role != UserRole.ADMIN) {
            throw NotAuthorizedException()
        }
        return userRepository.getUsers()
    }

    fun handleUserAuthentication(authentication: Authentication): User {
        val auth = authentication as? JwtAuthenticationToken ?: throw NotAuthorizedException("User not authorized")
        val claims = auth.token.claims

        val externalId = claims["sub"] as String
        userRepository.getUserByExternalId(externalId)?.let { return it }

        val firstName = claims["given_name"] as String
        val lastName = claims["family_name"] as String
        val email = claims["email"] as String

        val userFromIdToken = User(
                id = 0L,
                externalId = externalId,
                firstName = firstName,
                lastName = lastName,
                phoneNumber = "",
                email = email
        )

        return createUser(userFromIdToken).copy(firstLogin = true)
    }

    fun createUser(user: User): User {
        val newUserId = userRepository.save(user).toLong()
        userRepository.saveRoleForUser(newUserId)
        return user.copy(id = newUserId)
    }

    fun updateUser(updatedUser: UserInput, user: User?): User {
        user ?: throw NotAuthorizedException()

        val userToUpdate = user.copy(
                firstName = updatedUser.firstName,
                lastName = updatedUser.lastName,
                email = updatedUser.email
        )
        userRepository.update(userToUpdate)
        return userToUpdate
    }

    fun savePushToken(pushToken: String?, user: User?): Int? {
        user ?: throw NotAuthorizedException()
        val tokenSaved = userRepository.savePushToken(pushToken, user.id)

        if (tokenSaved == null) {
            throw GraphQLException("Could not save pushToken for user with id: $user.id")
        }
        return tokenSaved
    }
}