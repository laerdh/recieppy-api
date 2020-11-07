package com.ledahl.apps.recieppyapi.service

import com.ledahl.apps.recieppyapi.exception.NotAuthorizedException
import com.ledahl.apps.recieppyapi.model.User
import com.ledahl.apps.recieppyapi.model.UserProfile
import com.ledahl.apps.recieppyapi.model.enums.UserRole
import com.ledahl.apps.recieppyapi.repository.UserRepository
import graphql.GraphQLException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class UserService(@Autowired private val userRepository: UserRepository) {
    fun getUsers(user: User): List<User> {
        if (user.role != UserRole.ADMIN) {
            throw NotAuthorizedException()
        }
        return userRepository.getUsers()
    }

    fun getUserBySubject(subject: String): User? {
        return userRepository.getUserBySubject(subject)
    }

    fun getUsersInLocation(locationId: Long): List<UserProfile> {
        val members = mutableListOf<UserProfile>()

        members.addAll(userRepository.getUsersInLocation(locationId).map {
            mapToUserProfile(it)
        })
        members.addAll(userRepository.getUsersInvitedToLocation(locationId))

        return members
    }

    fun createUser(user: User): User {
        val newUserId = userRepository.save(user).toLong()
        userRepository.saveRoleForUser(newUserId)
        return user.copy(id = newUserId)
    }

    fun savePushToken(pushToken: String?, user: User): Int? {
        val tokenSaved = userRepository.savePushToken(pushToken, user.id)

        if (tokenSaved == null) {
            throw GraphQLException("Could not save pushToken for user with id: $user.id")
        }
        return tokenSaved
    }

    private fun mapToUserProfile(user: User): UserProfile {
        return UserProfile(
                id = user.id,
                firstName = user.firstName,
                lastName = user.lastName,
                email = user.email
        )
    }
}