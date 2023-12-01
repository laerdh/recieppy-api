package com.ledahl.apps.recieppyapi.controller

import com.ledahl.apps.recieppyapi.auth.JwtPrincipal
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
    fun user(@AuthenticationPrincipal jwtPrincipal: JwtPrincipal): User? {
        return jwtPrincipal.user
    }

    @QueryMapping
    fun users(@AuthenticationPrincipal jwtPrincipal: JwtPrincipal): List<User> {
        return userService.getUsers(jwtPrincipal.user)
    }

    @MutationMapping
    fun savePushToken(@AuthenticationPrincipal jwtPrincipal: JwtPrincipal, @Argument pushToken: String?): Int? {
        return userService.savePushToken(pushToken, jwtPrincipal.user)
    }

    @SchemaMapping
    fun locations(user: User): List<Location> {
        return locationService.getLocations(user)
    }
}