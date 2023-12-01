package com.ledahl.apps.recieppyapi.controller

import com.ledahl.apps.recieppyapi.auth.JwtPrincipal
import com.ledahl.apps.recieppyapi.model.Recipe
import com.ledahl.apps.recieppyapi.model.RecipePlan
import com.ledahl.apps.recieppyapi.model.RecipePlanEvent
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
    fun recipePlan(@AuthenticationPrincipal jwtPrincipal: JwtPrincipal, @Argument locationId: Long, @Argument weekNumber: Int): RecipePlan {
        return recipePlanService.getRecipePlanForWeek(
            user = jwtPrincipal.user,
            locationId = locationId,
            weekNumber = weekNumber
        )
    }

    @MutationMapping
    fun newRecipePlanEvent(@AuthenticationPrincipal jwtPrincipal: JwtPrincipal, @Argument locationId: Long, @Argument recipePlanEvent: RecipePlanEventInput): RecipePlan? {
        return recipePlanService.createRecipePlanEvent(
            user = jwtPrincipal.user,
            locationId = locationId,
            recipePlanEvent = recipePlanEvent
        )
    }

    @MutationMapping
    fun updateRecipePlanEvent(@AuthenticationPrincipal jwtPrincipal: JwtPrincipal, @Argument locationId: Long, @Argument recipePlanEvent: RecipePlanEventInput): RecipePlan? {
        return recipePlanService.updateRecipePlanEvent(
            user = jwtPrincipal.user,
            locationId = locationId,
            recipePlanEvent = recipePlanEvent
        )
    }

    @MutationMapping
    fun deleteRecipePlanEvent(@AuthenticationPrincipal jwtPrincipal: JwtPrincipal, @Argument locationId: Long, @Argument recipePlanEvent: RecipePlanEventInput): RecipePlan? {
        return recipePlanService.deleteRecipePlanEvent(
            user = jwtPrincipal.user,
            locationId = locationId,
            recipePlanEvent = recipePlanEvent
        )
    }

    @SchemaMapping
    fun events(@AuthenticationPrincipal jwtPrincipal: JwtPrincipal, recipePlan: RecipePlan): List<RecipePlanEvent> {
        return recipePlanService.getRecipePlanEventsByWeek(
            user = jwtPrincipal.user,
            locationId = recipePlan.locationId,
            weekNumber = recipePlan.weekNumber
        )
    }

    @SchemaMapping
    fun recipe(@AuthenticationPrincipal jwtPrincipal: JwtPrincipal, recipePlanEvent: RecipePlanEvent): Recipe? {
        return recipeService.getRecipeForRecipePlan(jwtPrincipal.user, recipePlanEvent.recipeId)
    }
}