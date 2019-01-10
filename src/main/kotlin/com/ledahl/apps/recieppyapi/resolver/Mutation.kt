package com.ledahl.apps.recieppyapi.resolver

import com.coxautodev.graphql.tools.GraphQLMutationResolver
import com.ledahl.apps.recieppyapi.model.*
import com.ledahl.apps.recieppyapi.model.input.RecipeInput
import com.ledahl.apps.recieppyapi.model.input.RecipeListInput
import com.ledahl.apps.recieppyapi.model.input.TagInput
import com.ledahl.apps.recieppyapi.repository.RecipeListRepository
import com.ledahl.apps.recieppyapi.repository.RecipeRepository
import com.ledahl.apps.recieppyapi.repository.TagRepository
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDate

class Mutation(@Autowired private val recipeRepository: RecipeRepository,
               @Autowired private val recipeListRepository: RecipeListRepository,
               @Autowired private val tagRepository: TagRepository) : GraphQLMutationResolver {
    fun newRecipeList(recipeList: RecipeListInput): RecipeList? {
        val newRecipeList = RecipeList(
                id = 0,
                name = recipeList.name,
                created = LocalDate.now()
        )

        val newRecipeListId = recipeListRepository.save(newRecipeList)
        if (newRecipeListId != 0) {
            recipeListRepository.saveListToUser(
                    recipeListId = newRecipeListId.toLong(),
                    userId = recipeList.creatorId
            )
            return newRecipeList.copy(id = newRecipeListId.toLong())
        }
        return null
    }

    fun newRecipe(recipe: RecipeInput): Recipe? {
        val newRecipe = Recipe(
                id = 0,
                title = recipe.title,
                url = recipe.url,
                imageUrl = recipe.imageUrl,
                site = recipe.site,
                recipeListId = recipe.recipeListId
        )

        val newRecipeId = recipeRepository.save(newRecipe)
        if (newRecipeId != 0) {
            recipe.tags?.let {
                recipeRepository.saveTagsToRecipe(
                        recipeId = newRecipeId.toLong(),
                        tags = it
                )
            }
            return newRecipe.copy(id = newRecipeId.toLong())
        }
        return null
    }

    fun newTag(tag: TagInput): Tag? {
        val newTag = Tag(
                id = 0,
                text = tag.text
        )

        val newTagId = tagRepository.save(newTag)
        if (newTagId != 0) {
            return newTag.copy(id = newTagId.toLong())
        }
        return null
    }
}