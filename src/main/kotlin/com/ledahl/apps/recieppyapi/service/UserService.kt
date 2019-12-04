package com.ledahl.apps.recieppyapi.service

import com.ledahl.apps.recieppyapi.exception.NotAuthorizedException
import com.ledahl.apps.recieppyapi.model.User
import com.ledahl.apps.recieppyapi.model.enums.UserRole
import com.ledahl.apps.recieppyapi.model.input.UserInput
import com.ledahl.apps.recieppyapi.repository.UserRepository
import graphql.GraphQLException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class UserService(@Autowired private val userRepository: UserRepository) {
    fun getUsers(user: User?): List<User> {
        if (user?.role != UserRole.ADMIN) {
            throw NotAuthorizedException()
        }
        return userRepository.getUsers()
    }

    fun getUserFromToken(token: String): User? {
        return userRepository.getUserFromToken(token)
    }

    fun getUserFromPhoneNumber(phoneNumber: String): User? {
        return userRepository.getUserFromPhoneNumber(phoneNumber)
    }

    fun createUser(phoneNumber: String, token: String): User {
        val newUser = User(
                firstName = "",
                lastName = "",
                phoneNumber = phoneNumber,
                token = token
        )
        val newUserId = userRepository.save(newUser).toLong()
        userRepository.saveRoleForUser(newUserId)
        return newUser.copy(id = newUserId)
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

    fun saveToken(user: User) {
        userRepository.saveTokenForUser(user)
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