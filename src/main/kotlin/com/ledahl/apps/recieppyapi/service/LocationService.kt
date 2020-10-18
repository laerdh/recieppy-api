package com.ledahl.apps.recieppyapi.service

import com.ledahl.apps.recieppyapi.model.Location
import com.ledahl.apps.recieppyapi.model.User
import com.ledahl.apps.recieppyapi.model.input.NewLocationInput
import com.ledahl.apps.recieppyapi.repository.LocationRepository
import graphql.GraphQLException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import java.util.*

@Service
class LocationService(@Autowired private val locationRepository: LocationRepository,
                      @Autowired private val imageService: ImageService) {
    fun createNewLocation(newLocationInput: NewLocationInput, user: User): Location? {
        val userId = user.id
        val inviteCode = createUniqueInviteCode()

        val coverImageUrl = imageService.getCoverImage()
        val locationId = locationRepository.createNewLocation(newLocationInput.name,
                newLocationInput.address,
                userId,
                inviteCode,
                coverImageUrl
        )

        if (locationId != null) {
            val locationUserAccountId = insertUserOnLocation(userId, locationId.toLong())

            if (locationUserAccountId != 0) {
                return Location(
                        id = locationId.toLong(),
                        name = newLocationInput.name,
                        address = newLocationInput.address,
                        owner = userId,
                        inviteCode = inviteCode,
                        imageUrl = coverImageUrl
                )
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

    @PreAuthorize("@authService.isOwnerOfLocation(#user, #locationId)")
    fun updateLocation(locationId: Long, updatedLocation: NewLocationInput, user: User): Location? {
        return locationRepository.updateLocation(
                locationId = locationId,
                name = updatedLocation.name,
                address = updatedLocation.address
        ) ?: throw GraphQLException("Could not update location with id $locationId")
    }

    @PreAuthorize("@authService.isMemberOfLocation(#user, #locationId)")
    fun removeCurrentUserFromLocation(user: User, locationId: Long): List<Location> {
        val loggedInUserId = user.id

        val location = locationRepository.getLocation(loggedInUserId, locationId)
        if (location?.owner == loggedInUserId) {
            throw GraphQLException("Could not remove user (id: $loggedInUserId) from location. User is owner.")
        }

        locationRepository.removeUserFromLocation(loggedInUserId, locationId)
        return locationRepository.getLocationsForUser(loggedInUserId)
    }

    @PreAuthorize("@authService.isOwnerOfLocation(#user, #locationId)")
    fun removeUserFromLocation(user: User, userId: Long, locationId: Long): Location? {
        val loggedInUserId = user.id

        if (loggedInUserId == userId) {
            throw GraphQLException("Could not remove user (id: $userId). User is owner")
        }

        locationRepository.removeUserFromLocation(userId, locationId)
        return locationRepository.getLocation(loggedInUserId, locationId)
    }

    fun acceptInviteForUser(user: User, inviteCode: String): Boolean {
        val locationIdForInviteCode = locationRepository.getLocationIdFromInviteCode(inviteCode)

        if (locationIdForInviteCode == null) {
            throw GraphQLException("Invite-code not valid")
        }

        val userInserted = insertUserOnLocation(user.id, locationIdForInviteCode)

        return userInserted.toInt() > 0
    }

    fun getLocation(user: User, locationId: Long): Location? {
        return locationRepository.getLocation(userId = user.id, locationId = locationId)
    }

    fun getLocations(user: User): List<Location> {
        return locationRepository.getLocationsForUser(user.id)
    }

    fun getLocationNameForInviteCode(inviteCode: String): String? {
        return locationRepository.getLocationNameFromInviteCode(inviteCode)
    }

    private fun createUniqueInviteCode(): String {
        val inviteCode = UUID.randomUUID().toString().substring(0, 6)
        val existingLocationForInviteCode = locationRepository.getLocationIdFromInviteCode(inviteCode)

        return if (existingLocationForInviteCode == null) {
            inviteCode
        } else createUniqueInviteCode()
    }
}