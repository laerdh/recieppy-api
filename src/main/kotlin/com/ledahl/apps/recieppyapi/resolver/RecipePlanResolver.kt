package com.ledahl.apps.recieppyapi.resolver

import com.coxautodev.graphql.tools.GraphQLResolver
import com.ledahl.apps.recieppyapi.model.RecipeEvent
import com.ledahl.apps.recieppyapi.model.RecipePlan
import com.ledahl.apps.recieppyapi.service.RecipePlanService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.*

@Component
class RecipePlanResolver(@Autowired private val recipePlanService: RecipePlanService): GraphQLResolver<RecipePlan> {
    fun getEvents(recipePlan: RecipePlan): List<RecipeEvent> {
        val currentWeek = Calendar.getInstance().get(Calendar.WEEK_OF_YEAR)
        return recipePlanService.getRecipeEventsByWeek(recipePlan.locationId, weekNumber = currentWeek)
    }
}