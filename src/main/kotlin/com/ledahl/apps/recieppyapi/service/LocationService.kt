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

    fun insertUserOnLocation(userId: Long, locationId: Long): Int {
        val existingLocationId = locationRepository.findLocationWithId(locationId = locationId)

        if (existingLocationId == null) {
            throw GraphQLException("Could not find location with id $locationId")
        }

        return locationRepository.addUserToLocation(userId, locationId).toInt()
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
    fun sendEmailInviteToUser(user: User, locationId: Long, toEmail: String): Location? {
        val inviteEmail = toEmail.lowercase().trim()

        val existingInvitation = locationRepository.getEmailInvite(user.id, locationId, inviteEmail)
        val location = locationRepository.getLocation(user.id, locationId)

        if (location == null) {
            throw GraphQLException("Location (locationId: $locationId) not found")
        }

        val inviteCode = createUniqueInviteCode()

        // Re-send invite
        if (existingInvitation != null) {
            val lastSentTimeLessThanOneDayAgo = existingInvitation.timeSent?.isAfter(LocalDateTime.now().minusDays(1)) ?: false
            if (lastSentTimeLessThanOneDayAgo) {
                throw GraphQLException("User '$inviteEmail' has already been invited to location")
            }
        } else {
            val emailInviteInserted = locationRepository.createEmailInvite(
                    userId = user.id,
                    locationId = location.id,
                    email = inviteEmail,
                    inviteCode = inviteCode
            )

            if (!emailInviteInserted) {
                throw GraphQLException("Could not create invite (locationId: $locationId, email: $inviteEmail)")
            }
        }

        emailService.sendInvite(
                fromName = "${user.firstName} ${user.lastName}",
                toEmail = inviteEmail,
                locationName = location.name,
                inviteCode = inviteCode
        ).thenAccept { didSend ->
            if (didSend) {
                locationRepository.updateEmailInviteTimeSent(locationId, inviteCode, LocalDateTime.now())
            }
        }

        return location
    }

    @PreAuthorize("@authService.isOwnerOfLocation(#user, #locationId)")
    fun revokeEmailInvite(user: User, locationId: Long, email: String): Location? {
        val inviteEmail = email.lowercase().trim()
        val existingInvite = locationRepository.getEmailInvite(user.id, locationId, inviteEmail)

        if (existingInvite == null) {
            throw GraphQLException("Could not revoke invitation. Invitation does not exist")
        }

        if (existingInvite.acceptedByUser != null) {
            throw GraphQLException("Invite code is already used")
        }

        locationRepository.removeEmailInvite(user.id, locationId, email)
        return locationRepository.getLocation(user.id, locationId)
    }

    fun acceptInviteForUser(user: User, inviteCode: String): Location? {
        val validatedInviteCode = validateInviteCode(inviteCode)

        val invitedLocation = locationRepository.getLocationFromInviteCode(validatedInviteCode)
        if (invitedLocation == null) {
            throw GraphQLException("Invite-code '$validatedInviteCode' is invalid")
        }

        val isMemberOfLocation = locationRepository.isUserMemberOfLocation(user.id, invitedLocation.id)
        if (isMemberOfLocation) {
            throw GraphQLException("User is already member of location")
        }

        val userInserted = insertUserOnLocation(user.id, invitedLocation.id) > 0
        if (!userInserted) {
            throw GraphQLException("Could not accept inviteCode")
        }

        val emailInvitation = locationRepository.getEmailInvite(validatedInviteCode)
        if (emailInvitation != null) {
            locationRepository.updateEmailInviteAccepted(
                    locationId = invitedLocation.id,
                    inviteCode = validatedInviteCode,
                    userId = user.id
            )
        }

        return invitedLocation
    }

    fun getLocations(user: User): List<Location> {
        return locationRepository.getLocationsForUser(user.id)
    }

    fun getLocationNameForInviteCode(inviteCode: String): String? {
        val validatedInviteCode = validateInviteCode(inviteCode)
        return locationRepository.getLocationNameFromInviteCode(validatedInviteCode)
    }

    private fun validateInviteCode(inviteCode: String): String {
        val trimmedInviteCode = inviteCode.trim()
        if (trimmedInviteCode.isEmpty()) {
            throw GraphQLException("Invite-code not valid")
        }
        return trimmedInviteCode.uppercase()
    }

    private fun createUniqueInviteCode(): String {
        val inviteCode = UUID.randomUUID().toString().substring(0, 6).uppercase()
        val existingLocationForInviteCode = locationRepository.getLocationIdFromInviteCode(inviteCode)

        return if (existingLocationForInviteCode == null) {
            inviteCode
        } else createUniqueInviteCode()
    }
}