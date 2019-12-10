package com.ledahl.apps.recieppyapi.service

import com.ledahl.apps.recieppyapi.exception.NotAuthorizedException
import com.ledahl.apps.recieppyapi.model.User
import com.ledahl.apps.recieppyapi.model.input.NewLocationInput
import com.ledahl.apps.recieppyapi.repository.LocationRepository
import graphql.GraphQLException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*

@Service
class LocationService(
        @Autowired private val locationRepository: LocationRepository
) {
    fun createNewLocation(newLocationInput: NewLocationInput, user: User?): Long {
        val userId = user?.id ?: throw NotAuthorizedException()

        val inviteCode = UUID.randomUUID().toString().substring(0, 6)

        val locationId = locationRepository.createNewLocation(newLocationInput.name,
                newLocationInput.address,
                userId,
                inviteCode).toLong()

        if (locationId != -1L) {
            val locationUserAccountId = insertUserOnLocation(userId, locationId)

            if (locationUserAccountId != -1L) {
                return locationUserAccountId.toLong()
            } else {
                throw GraphQLException("Could not insert userId $userId to location ${newLocationInput.name}")
            }
        } else {
            throw GraphQLException("Could not create new location with name ${newLocationInput.name} for userId $userId")
        }
    }

    fun insertUserOnLocation(userId: Long, locationId: Long): Number {
        val existingLocationId = locationRepository.findLocationWithId(locationId = locationId)

        if (existingLocationId == null) {
            throw GraphQLException("Could not find location with id $locationId")
        }

        return locationRepository.addUserToLocation(userId, locationId)
    }

    fun getInviteCode(user: User?): String {
        val userId = user?.id ?: throw NotAuthorizedException()

        val locations = locationRepository.getLocationsForUser(userId)

        if (locations.isEmpty()) {
            throw GraphQLException("User has no locations")
        }

        val existingCodeForLocation = locationRepository.getInviteCode(locations.first())

        if (existingCodeForLocation == null) {
            throw GraphQLException("Couldn't get inviteCode for location")
        }

        return existingCodeForLocation
    }

    fun acceptInviteForUser(user: User?, inviteCode: String): Boolean {
        val userId = user?.id ?: throw NotAuthorizedException()

        val locationIdForInviteCode = locationRepository.getLocationFromInviteCode(inviteCode)

        if (locationIdForInviteCode == null) {
            throw GraphQLException("Invite-code not valid")
        }

        val userInserted = insertUserOnLocation(userId, locationIdForInviteCode)

        return userInserted.toInt() > 0
    }
}