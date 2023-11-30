package com.ledahl.apps.recieppyapi.controller

import com.ledahl.apps.recieppyapi.auth.JwtPrincipal
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
    fun locations(@AuthenticationPrincipal jwtPrincipal: JwtPrincipal): List<Location> {
        return locationService.getLocations(jwtPrincipal.user)
    }

    @QueryMapping
    fun locationForInviteCode(@Argument inviteCode: String): String? {
        return locationService.getLocationNameForInviteCode(inviteCode)
    }

    @MutationMapping
    fun newLocation(@AuthenticationPrincipal jwtPrincipal: JwtPrincipal, @Argument input: NewLocationInput): Location? {
        return locationService.createNewLocation(input, jwtPrincipal.user)
    }

    @MutationMapping
    fun updateLocation(@AuthenticationPrincipal jwtPrincipal: JwtPrincipal, @Argument locationId: Long, @Argument input: NewLocationInput): Location? {
        return locationService.updateLocation(
            locationId = locationId,
            updatedLocation = input,
            user = jwtPrincipal.user
        )
    }

    @MutationMapping
    fun removeCurrentUserFromLocation(@AuthenticationPrincipal jwtPrincipal: JwtPrincipal, @Argument locationId: Long): List<Location> {
        return locationService.removeCurrentUserFromLocation(jwtPrincipal.user, locationId)
    }

    @MutationMapping
    fun removeUsersFromLocation(@AuthenticationPrincipal jwtPrincipal: JwtPrincipal, @Argument userIds: List<Long>, @Argument locationId: Long): Location? {
        return locationService.removeUsersFromLocation(
            user = jwtPrincipal.user,
            userIds = userIds,
            locationId = locationId
        )
    }

    @MutationMapping
    fun acceptInvite(@AuthenticationPrincipal jwtPrincipal: JwtPrincipal, @Argument inviteCode: String): Location? {
        return locationService.acceptInviteForUser(jwtPrincipal.user, inviteCode)
    }

    @MutationMapping
    fun sendInvite(@AuthenticationPrincipal jwtPrincipal: JwtPrincipal, @Argument locationId: Long, @Argument email: String): Location? {
        return locationService.sendEmailInviteToUser(
            user = jwtPrincipal.user,
            locationId = locationId,
            toEmail = email
        )
    }

    @MutationMapping
    fun revokeInvite(@AuthenticationPrincipal jwtPrincipal: JwtPrincipal, @Argument locationId: Long, @Argument email: String): Location? {
        return locationService.revokeEmailInvite(
            user = jwtPrincipal.user,
            locationId = locationId,
            email = email
        )
    }

    @SchemaMapping
    fun recipeLists(@AuthenticationPrincipal jwtPrincipal: JwtPrincipal, location: Location): List<RecipeList> {
        return recipeListService.getRecipeListsForUser(jwtPrincipal.user, location.id)
    }

    @SchemaMapping
    fun recipePlan(@AuthenticationPrincipal jwtPrincipal: JwtPrincipal, location: Location): RecipePlan {
        return recipePlanService.getRecipePlanForCurrentWeek(jwtPrincipal.user, location.id)
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