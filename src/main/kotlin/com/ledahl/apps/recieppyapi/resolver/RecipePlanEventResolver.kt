package com.ledahl.apps.recieppyapi.resolver

import com.coxautodev.graphql.tools.GraphQLResolver
import com.ledahl.apps.recieppyapi.model.Recipe
import com.ledahl.apps.recieppyapi.model.RecipePlanEvent
import com.ledahl.apps.recieppyapi.service.RecipeService
import graphql.schema.DataFetchingEnvironment
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class RecipePlanEventResolver(@Autowired private val recipeService: RecipeService): GraphQLResolver<RecipePlanEvent> {
    fun getRecipe(recipePlanEvent: RecipePlanEvent, env: DataFetchingEnvironment): Recipe? {
        return recipeService.getRecipe(recipeId = recipePlanEvent.recipeId)
    }
}