package com.ledahl.apps.recieppyapi.resolver

import com.coxautodev.graphql.tools.GraphQLResolver
import com.ledahl.apps.recieppyapi.auth.context.AuthContext
import com.ledahl.apps.recieppyapi.model.Location
import com.ledahl.apps.recieppyapi.model.RecipeList
import com.ledahl.apps.recieppyapi.model.RecipePlan
import com.ledahl.apps.recieppyapi.service.RecipeListService
import com.ledahl.apps.recieppyapi.service.RecipePlanService
import graphql.schema.DataFetchingEnvironment
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class LocationResolver(@Autowired private val recipeListService: RecipeListService,
                       @Autowired private val recipePlanService: RecipePlanService): GraphQLResolver<Location> {
    fun getRecipeLists(location: Location, env: DataFetchingEnvironment): List<RecipeList> {
        val user = env.getContext<AuthContext>().user
        return recipeListService.getRecipeListsForUser(user, location.id.toInt())
    }

    fun getRecipePlan(location: Location): RecipePlan {
        return recipePlanService.getRecipePlanForCurrentWeek(location.id)
    }
}