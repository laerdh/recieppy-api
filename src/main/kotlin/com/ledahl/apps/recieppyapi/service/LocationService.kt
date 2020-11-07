package com.ledahl.apps.recieppyapi.service

import com.ledahl.apps.recieppyapi.model.Location
import com.ledahl.apps.recieppyapi.model.User
import com.ledahl.apps.recieppyapi.model.input.NewLocationInput
import com.ledahl.apps.recieppyapi.repository.LocationRepository
import graphql.GraphQLException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.*

@Service
class LocationService(@Autowired private val locationRepository: LocationRepository,
                      @Autowired private val imageService: ImageService,
                      @Autowired private val emailService: EmailService) {

    private val logger = LoggerFactory.getLogger(LocationService::class.java)

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

        locationRepository.removeUsersFromLocation(listOf(loggedInUserId), locationId)
        return locationRepository.getLocationsForUser(loggedInUserId)
    }

    @PreAuthorize("@authService.isOwnerOfLocation(#user, #locationId)")
    fun removeUsersFromLocation(user: User, userIds: List<Long>, locationId: Long): Location? {
        val loggedInUserId = user.id

        var userIdsToRemove = userIds
        if (userIds.contains(loggedInUserId)) {
            userIdsToRemove = userIds.filter { it != loggedInUserId }
            logger.info("Provided list contains owner (id: $loggedInUserId). Removing owner from list.")
        }

        locationRepository.removeUsersFromLocation(userIdsToRemove, locationId)
        return locationRepository.getLocation(loggedInUserId, locationId)
    }

    @PreAuthorize("@authService.isOwnerOfLocation(#user, #locationId)")
    fun sendEmailInviteToUser(user: User, locationId: Long, toEmail: String): Boolean {
        val fromUser = "${user.firstName} ${user.lastName}"
        val inviteEmail = toEmail.toLowerCase().trim()

        val existingInvitation = locationRepository.getLocationEmailInvite(locationId, inviteEmail)
        val location = locationRepository.getLocation(user.id, locationId)

        if (location == null) {
            throw GraphQLException("Location (locationId: $locationId) not found")
        }

        val inviteCode = createUniqueInviteCode()

        // Re-send invite
        if (existingInvitation != null) {
            val lastSentTimeLessThanOneDay = existingInvitation.sent.isAfter(LocalDateTime.now().minusDays(1))
            if (lastSentTimeLessThanOneDay) {
                throw GraphQLException("An invite has already been sent to $inviteEmail at ${existingInvitation.sent}")
            }

            return emailService.sendInvite(
                    fromName = fromUser,
                    toEmail = inviteEmail,
                    locationName = location.name,
                    inviteCode = inviteCode
            )
        }

        val emailInviteInserted = locationRepository.insertLocationEmailInvite(
                locationId = location.id,
                email = inviteEmail,
                inviteCode = inviteCode
        )

        if (emailInviteInserted) {
            return emailService.sendInvite(
                    fromName = fromUser,
                    toEmail = inviteEmail,
                    locationName = location.name,
                    inviteCode = inviteCode
            )
        }

        throw GraphQLException("Could not send invite (locationId: $locationId, email: $inviteEmail)")
    }

    fun acceptInviteForUser(user: User, inviteCode: String): Location? {
        var locationForInviteCode = locationRepository.getLocationFromInviteCode(inviteCode)

        if (locationForInviteCode == null) {
            locationForInviteCode = locationRepository.getLocationFromEmailInviteCode(inviteCode)?.also {
                locationRepository.updateLocationEmailInvite(it.id, inviteCode, user.id)
            }
        }

        if (locationForInviteCode == null) {
            throw GraphQLException("Invite-code not valid")
        }

        val userInserted = insertUserOnLocation(user.id, locationForInviteCode.id)

        if (userInserted.toInt() < 1) {
            throw GraphQLException("Could not add user to location. User may have been added already.")
        }

        return locationForInviteCode
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