package com.ledahl.apps.recieppyapi.resolver

import com.coxautodev.graphql.tools.GraphQLResolver
import com.ledahl.apps.recieppyapi.auth.context.AuthContext
import com.ledahl.apps.recieppyapi.model.RecipePlanEvent
import com.ledahl.apps.recieppyapi.model.RecipePlan
import com.ledahl.apps.recieppyapi.service.RecipePlanService
import graphql.schema.DataFetchingEnvironment
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class RecipePlanResolver(@Autowired private val recipePlanService: RecipePlanService): GraphQLResolver<RecipePlan> {
    fun getEvents(recipePlan: RecipePlan, env: DataFetchingEnvironment): List<RecipePlanEvent> {
        val user = env.getContext<AuthContext>().user
        return recipePlanService.getRecipePlanEventsByWeek(
                user = user,
                locationId = recipePlan.locationId,
                weekNumber = recipePlan.weekNumber
        )
    }
}