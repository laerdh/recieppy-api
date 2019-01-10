package com.ledahl.apps.recieppyapi.resolver

import com.coxautodev.graphql.tools.GraphQLMutationResolver
import com.ledahl.apps.recieppyapi.model.Recipe
import com.ledahl.apps.recieppyapi.model.RecipeList
import com.ledahl.apps.recieppyapi.repository.RecipeListRepository
import com.ledahl.apps.recieppyapi.repository.RecipeRepository
import org.springframework.beans.factory.annotation.Autowired

class Mutation(@Autowired private val recipeRepository: RecipeRepository,
               @Autowired private val recipeListRepository: RecipeListRepository) : GraphQLMutationResolver {
    fun newRecipeList(name: String, userId: Long): RecipeList? {
        val recipeList = RecipeList(name = name)
        val recipeListId = recipeListRepository.save(recipeList)

        if (recipeListId != 0) {
            recipeListRepository.saveListToUser(recipeListId = recipeListId.toLong(), userId = userId)
            return recipeList.copy(id = recipeListId.toLong())
        }
        return null
    }

    fun newRecipe(title: String, url: String, imageUrl: String, site: String, recipeListId: Long): Recipe? {
        val recipe = Recipe(
                id = 0,
                title = title,
                url = url,
                imageUrl = imageUrl,
                site = site,
                recipeListId = recipeListId
        )

        val recipeId = recipeRepository.save(recipe)
        if (recipeId != 0) {
            return recipe.copy(id = recipeId.toLong())
        }
        return null
    }
}