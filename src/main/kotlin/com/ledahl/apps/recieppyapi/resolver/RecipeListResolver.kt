package com.ledahl.apps.recieppyapi.resolver

import com.coxautodev.graphql.tools.GraphQLResolver
import com.ledahl.apps.recieppyapi.model.Recipe
import com.ledahl.apps.recieppyapi.model.RecipeList
import com.ledahl.apps.recieppyapi.service.RecipeService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class RecipeListResolver(@Autowired private val recipeService: RecipeService): GraphQLResolver<RecipeList> {
    fun getRecipes(recipeList: RecipeList): List<Recipe> {
        return recipeService.getRecipesForRecipeList(recipeList = recipeList)
    }
}