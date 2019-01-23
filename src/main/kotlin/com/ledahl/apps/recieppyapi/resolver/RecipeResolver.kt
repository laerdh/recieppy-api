package com.ledahl.apps.recieppyapi.resolver

import com.coxautodev.graphql.tools.GraphQLResolver
import com.ledahl.apps.recieppyapi.model.Recipe
import com.ledahl.apps.recieppyapi.model.Tag
import com.ledahl.apps.recieppyapi.service.RecipeService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class RecipeResolver(@Autowired private val recipeService: RecipeService): GraphQLResolver<Recipe> {
    fun getTags(recipe: Recipe): List<Tag>? {
        return recipeService.getTagsForRecipe(recipe.id)
    }
}