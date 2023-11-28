package com.ledahl.apps.recieppyapi.controller

import com.ledahl.apps.recieppyapi.model.Recipe
import com.ledahl.apps.recieppyapi.model.RecipePlan
import com.ledahl.apps.recieppyapi.model.RecipePlanEvent
import com.ledahl.apps.recieppyapi.model.User
import com.ledahl.apps.recieppyapi.model.input.RecipePlanEventInput
import com.ledahl.apps.recieppyapi.service.RecipePlanService
import com.ledahl.apps.recieppyapi.service.RecipeService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.graphql.data.method.annotation.SchemaMapping
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller

@Controller
class RecipePlanController(@Autowired private val recipePlanService: RecipePlanService,
                           @Autowired private val recipeService: RecipeService) {
    @QueryMapping
    fun recipePlan(@AuthenticationPrincipal user: User, @Argument locationId: Long, @Argument weekNumber: Int): RecipePlan {
        return recipePlanService.getRecipePlanForWeek(
            user = user,
            locationId = locationId,
            weekNumber = weekNumber
        )
    }

    @MutationMapping
    fun newRecipePlanEvent(@AuthenticationPrincipal user: User, @Argument locationId: Long, @Argument recipePlanEvent: RecipePlanEventInput): RecipePlan? {
        return recipePlanService.createRecipePlanEvent(
            user = user,
            locationId = locationId,
            recipePlanEvent = recipePlanEvent
        )
    }

    @MutationMapping
    fun updateRecipePlanEvent(@AuthenticationPrincipal user: User, @Argument locationId: Long, @Argument recipePlanEvent: RecipePlanEventInput): RecipePlan? {
        return recipePlanService.updateRecipePlanEvent(
            user = user,
            locationId = locationId,
            recipePlanEvent = recipePlanEvent
        )
    }

    @MutationMapping
    fun deleteRecipePlanEvent(@AuthenticationPrincipal user: User, @Argument locationId: Long, @Argument recipePlanEvent: RecipePlanEventInput): RecipePlan? {
        return recipePlanService.deleteRecipePlanEvent(
            user = user,
            locationId = locationId,
            recipePlanEvent = recipePlanEvent
        )
    }

    @SchemaMapping
    fun events(@AuthenticationPrincipal user: User, recipePlan: RecipePlan): List<RecipePlanEvent> {
        return recipePlanService.getRecipePlanEventsByWeek(
            user = user,
            locationId = recipePlan.locationId,
            weekNumber = recipePlan.weekNumber
        )
    }

    @SchemaMapping
    fun recipe(@AuthenticationPrincipal user: User, recipePlanEvent: RecipePlanEvent): Recipe? {
        return recipeService.getRecipeForRecipePlan(user, recipePlanEvent.recipeId)
    }
}