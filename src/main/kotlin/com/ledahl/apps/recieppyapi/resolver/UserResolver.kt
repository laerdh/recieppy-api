package com.ledahl.apps.recieppyapi.resolver

import com.coxautodev.graphql.tools.GraphQLResolver
import com.ledahl.apps.recieppyapi.model.Location
import com.ledahl.apps.recieppyapi.model.User
import com.ledahl.apps.recieppyapi.service.LocationService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class UserResolver(@Autowired private val locationService: LocationService): GraphQLResolver<User> {
    fun getLocations(user: User): List<Location> {
        return locationService.getLocations(user)
    }
}