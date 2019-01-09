package com.ledahl.apps.recieppyapi.resolver

import com.coxautodev.graphql.tools.GraphQLQueryResolver
import com.ledahl.apps.recieppyapi.model.Recipe
import com.ledahl.apps.recieppyapi.model.Tag
import com.ledahl.apps.recieppyapi.repository.RecipeRepository
import com.ledahl.apps.recieppyapi.repository.TagRepository
import org.springframework.beans.factory.annotation.Autowired

class Query(@Autowired private val recipeRepository: RecipeRepository,
            @Autowired private val tagRepository: TagRepository) : GraphQLQueryResolver {
    fun getRecipe(id: Long): Recipe? {
        return recipeRepository.getRecipe(id)
    }

    fun getRecipes(): List<Recipe> {
        return recipeRepository.getRecipes()
    }

    fun getTags(): List<Tag> {
        return tagRepository.getTags()
    }
}