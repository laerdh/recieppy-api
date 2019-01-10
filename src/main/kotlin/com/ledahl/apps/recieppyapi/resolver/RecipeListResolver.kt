package com.ledahl.apps.recieppyapi.resolver

import com.coxautodev.graphql.tools.GraphQLResolver
import com.ledahl.apps.recieppyapi.model.Recipe
import com.ledahl.apps.recieppyapi.model.RecipeList
import com.ledahl.apps.recieppyapi.repository.RecipeRepository
import org.springframework.beans.factory.annotation.Autowired

class RecipeListResolver(@Autowired private val recipeRepository: RecipeRepository): GraphQLResolver<RecipeList> {
    fun getRecipes(recipeList: RecipeList): List<Recipe> {
        return recipeRepository.getRecipesForRecipeList(recipeList.id)
    }
}