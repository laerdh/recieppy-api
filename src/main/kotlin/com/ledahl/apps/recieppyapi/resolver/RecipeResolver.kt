package com.ledahl.apps.recieppyapi.resolver

import com.coxautodev.graphql.tools.GraphQLResolver
import com.ledahl.apps.recieppyapi.model.Recipe
import com.ledahl.apps.recieppyapi.model.Tag
import com.ledahl.apps.recieppyapi.repository.TagRepository
import org.springframework.beans.factory.annotation.Autowired

class RecipeResolver(@Autowired private val tagRepository: TagRepository): GraphQLResolver<Recipe> {
    fun getTags(recipe: Recipe): List<Tag>? {
        return tagRepository.getTagsForRecipe(recipe.id)
    }
}