package com.ledahl.apps.recieppyapi.controller

import com.ledahl.apps.recieppyapi.model.*
import com.ledahl.apps.recieppyapi.model.input.NewLocationInput
import com.ledahl.apps.recieppyapi.service.LocationService
import com.ledahl.apps.recieppyapi.service.RecipeListService
import com.ledahl.apps.recieppyapi.service.RecipePlanService
import com.ledahl.apps.recieppyapi.service.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.graphql.data.method.annotation.SchemaMapping
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller

@Controller
class LocationController(@Autowired private val locationService: LocationService,
                         @Autowired private val userService: UserService,
                         @Autowired private val recipeListService: RecipeListService,
                         @Autowired private val recipePlanService: RecipePlanService) {
    @QueryMapping
    fun locations(@AuthenticationPrincipal user: User): List<Location> {
        return locationService.getLocations(user)
    }

    @QueryMapping
    fun locationForInviteCode(@Argument inviteCode: String): String? {
        return locationService.getLocationNameForInviteCode(inviteCode)
    }

    @MutationMapping
    fun newLocation(@AuthenticationPrincipal user: User, @Argument input: NewLocationInput): Location? {
        return locationService.createNewLocation(input, user)
    }

    @MutationMapping
    fun updateLocation(@AuthenticationPrincipal user: User, @Argument locationId: Long, @Argument input: NewLocationInput): Location? {
        return locationService.updateLocation(
            locationId = locationId,
            updatedLocation = input,
            user = user
        )
    }

    @MutationMapping
    fun removeCurrentUserFromLocation(@AuthenticationPrincipal user: User, @Argument locationId: Long): List<Location> {
        return locationService.removeCurrentUserFromLocation(user, locationId)
    }

    @MutationMapping
    fun removeUsersFromLocation(@AuthenticationPrincipal user: User, @Argument userIds: List<Long>, @Argument locationId: Long): Location? {
        return locationService.removeUsersFromLocation(
            user = user,
            userIds = userIds,
            locationId = locationId
        )
    }

    @MutationMapping
    fun acceptInvite(@AuthenticationPrincipal user: User, @Argument inviteCode: String): Location? {
        return locationService.acceptInviteForUser(user, inviteCode)
    }

    @MutationMapping
    fun sendInvite(@AuthenticationPrincipal user: User, @Argument locationId: Long, @Argument email: String): Location? {
        return locationService.sendEmailInviteToUser(
            user = user,
            locationId = locationId,
            toEmail = email
        )
    }

    @MutationMapping
    fun revokeInvite(@AuthenticationPrincipal user: User, @Argument locationId: Long, @Argument email: String): Location? {
        return locationService.revokeEmailInvite(
            user = user,
            locationId = locationId,
            email = email
        )
    }

    @SchemaMapping
    fun recipeLists(@AuthenticationPrincipal user: User, location: Location): List<RecipeList> {
        return recipeListService.getRecipeListsForUser(user, location.id)
    }

    @SchemaMapping
    fun recipePlan(@AuthenticationPrincipal user: User, location: Location): RecipePlan {
        return recipePlanService.getRecipePlanForCurrentWeek(user, location.id)
    }

    @SchemaMapping
    fun members(location: Location): List<UserProfile> {
        return userService.getUsersInLocation(location.id)
    }

    @SchemaMapping
    fun invited(location: Location): List<UserProfile> {
        return userService.getUsersInvitedToLocation(location.id)
    }
}