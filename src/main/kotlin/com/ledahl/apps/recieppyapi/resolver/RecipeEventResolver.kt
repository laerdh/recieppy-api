package com.ledahl.apps.recieppyapi.resolver

import com.coxautodev.graphql.tools.GraphQLResolver
import com.ledahl.apps.recieppyapi.auth.context.AuthContext
import com.ledahl.apps.recieppyapi.model.Recipe
import com.ledahl.apps.recieppyapi.model.RecipeEvent
import com.ledahl.apps.recieppyapi.service.RecipeService
import graphql.schema.DataFetchingEnvironment
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class RecipeEventResolver(@Autowired private val recipeService: RecipeService): GraphQLResolver<RecipeEvent> {
    fun getRecipe(recipeEvent: RecipeEvent, env: DataFetchingEnvironment): Recipe? {
        val user = env.getContext<AuthContext>().user
        return recipeService.getRecipe(id = recipeEvent.recipeId, user = user)
    }
}