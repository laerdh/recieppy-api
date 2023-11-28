package com.ledahl.apps.recieppyapi.controller

import com.ledahl.apps.recieppyapi.model.Location
import com.ledahl.apps.recieppyapi.model.User
import com.ledahl.apps.recieppyapi.service.AuthService
import com.ledahl.apps.recieppyapi.service.LocationService
import com.ledahl.apps.recieppyapi.service.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.graphql.data.method.annotation.SchemaMapping
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller

@Controller
class UserController(@Autowired private val authService: AuthService,
                     @Autowired private val userService: UserService,
                     @Autowired private val locationService: LocationService) {
    @QueryMapping
    fun getUsers(@AuthenticationPrincipal user: User): List<User> {
        return userService.getUsers(user)
    }

    @QueryMapping
    fun getUser(@AuthenticationPrincipal user: User): User? {
        return user
    }

    @MutationMapping
    fun savePushToken(@AuthenticationPrincipal user: User, @Argument pushToken: String?): Int? {
        return userService.savePushToken(pushToken, user)
    }

    @SchemaMapping
    fun locations(user: User): List<Location> {
        return locationService.getLocations(user)
    }
}