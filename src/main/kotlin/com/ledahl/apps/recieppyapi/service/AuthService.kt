package com.ledahl.apps.recieppyapi.service

import com.ledahl.apps.recieppyapi.exception.NotAuthorizedException
import com.ledahl.apps.recieppyapi.model.User
import com.ledahl.apps.recieppyapi.repository.LocationRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Service

@Service
class AuthService(@Autowired private val userService: UserService,
                  @Autowired private val locationRepository: LocationRepository) {
    fun handleUserAuthentication(authentication: Authentication): User {
        val auth = authentication as? JwtAuthenticationToken ?: throw NotAuthorizedException("User not authorized")
        val claims = auth.token.claims

        val subject = claims["sub"] as String
        userService.getUserBySubject(subject)?.let { return it }

        val firstName = claims["given_name"] as String
        val lastName = claims["family_name"] as String
        val email = claims["email"] as String

        val userFromIdToken = User(
                id = 0L,
                subject = subject,
                firstName = firstName,
                lastName = lastName,
                phoneNumber = "",
                email = email
        )

        return userService.createUser(userFromIdToken).copy(firstLogin = true)
    }

    fun isMemberOfLocation(user: User, locationId: Long): Boolean {
        return locationRepository.isUserMemberOfLocation(userId = user.id, locationId = locationId)
    }

    fun isRecipeListInUsersLocation(user: User, recipeListId: Long): Boolean {
        return locationRepository.isRecipeListInUsersLocation(userId = user.id, recipeListId = recipeListId)
    }

    fun isRecipeInUsersLocation(user: User, recipeId: Long): Boolean {
        return locationRepository.isRecipeInUsersLocation(userId = user.id, recipeId = recipeId)
    }
}