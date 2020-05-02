package com.ledahl.apps.recieppyapi.service

import com.ledahl.apps.recieppyapi.model.Recipe
import com.ledahl.apps.recieppyapi.model.RecipeList
import com.ledahl.apps.recieppyapi.model.Tag
import com.ledahl.apps.recieppyapi.model.User
import com.ledahl.apps.recieppyapi.model.input.RecipeInput
import com.ledahl.apps.recieppyapi.model.input.TagInput
import com.ledahl.apps.recieppyapi.repository.RecipeListRepository
import com.ledahl.apps.recieppyapi.repository.RecipeRepository
import com.ledahl.apps.recieppyapi.repository.TagRepository
import graphql.GraphQLException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service

@Service
class RecipeService(@Autowired private val recipeRepository: RecipeRepository,
                    @Autowired private val recipeListRepository: RecipeListRepository,
                    @Autowired private val tagRepository: TagRepository) {

    private val logger = LoggerFactory.getLogger(RecipeService::class.java)

    @PreAuthorize("@authService.isRecipeAvailableToUser(#user, #recipeId)")
    fun getRecipe(user: User, recipeId: Long): Recipe? {
        return recipeRepository.getRecipe(userId = user.id, recipeId = recipeId)
    }

    fun getRecipeForRecipePlan(user: User, recipeId: Long): Recipe? {
        return recipeRepository.getRecipe(userId = user.id, recipeId = recipeId)
    }

    @PreAuthorize("@authService.isMemberOfLocation(#user, #locationId)")
    fun getRecipesForUser(user: User, locationId: Long): List<Recipe> {
        return recipeRepository.getRecipesForUser(userId = user.id, locationId = locationId)
    }

    fun getRecipesForRecipeList(userId: Long, recipeList: RecipeList): List<Recipe> {
        return recipeRepository.getRecipesForRecipeList(userId = userId, recipeListId = recipeList.id)
    }

    fun getTags(): List<Tag> {
        return tagRepository.getTags()
    }

    fun getTagsForRecipe(recipeId: Long): List<Tag> {
        return tagRepository.getTagsForRecipe(recipeId)
    }

    @PreAuthorize("@authService.isRecipeListEditableForUser(#user, #recipeInput.recipeListId)")
    fun createRecipe(user: User, recipeInput: RecipeInput): Recipe? {
        val recipeId = recipeRepository.createRecipe(userId = user.id, recipe = recipeInput)
        if (recipeId != 0) {
            val newRecipeId = recipeId.toLong()
            recipeRepository.addRecipeToRecipeList(recipeId = newRecipeId, recipeListId = recipeInput.recipeListId)

            recipeInput.tags?.let {
                tagRepository.saveTagsForRecipe(recipeId = newRecipeId, tags = it)
            }

            return Recipe(
                    id = newRecipeId,
                    recipeListId = recipeInput.recipeListId,
                    title = recipeInput.title,
                    url = recipeInput.url,
                    imageUrl = recipeInput.imageUrl,
                    site = recipeInput.site,
                    comment = recipeInput.comment
            )
        }

        throw GraphQLException("Could not create recipe")
    }

    @PreAuthorize("@authService.isRecipeEditableForUser(#user, #recipeId)")
    fun updateRecipe(user: User, recipeId: Long, recipeInput: RecipeInput): Recipe? {
        recipeListRepository.getRecipeList(userId = user.id, recipeListId = recipeInput.recipeListId)
                ?: throw IllegalArgumentException("No recipe list with id ${recipeInput.recipeListId} for user")

        val recipe = recipeRepository.getRecipe(userId = user.id, recipeId = recipeId) ?: throw GraphQLException("No recipe with id $recipeId found")

        if (recipe.recipeListId != recipeInput.recipeListId) {
            val deleted = recipeRepository.deleteRecipeFromRecipeList(recipeId = recipe.id, recipeListId = recipe.recipeListId)
            if (deleted > 0) {
                recipeRepository.addRecipeToRecipeList(recipeId = recipe.id, recipeListId = recipeInput.recipeListId)
            }
        }

        tagRepository.deleteTagsForRecipe(recipeId)

        val updatedRecipe = Recipe(
                id = recipeId,
                recipeListId = recipeInput.recipeListId,
                title = recipeInput.title,
                url = recipeInput.url,
                imageUrl = recipeInput.url,
                site = recipeInput.site,
                comment = recipeInput.comment
        )

        val recipeUpdated = recipeRepository.updateRecipe(updatedRecipe)
        if (recipeUpdated > 0) {
            recipeInput.tags?.let {
                tagRepository.saveTagsForRecipe(recipeId = recipeId, tags = it)
            }

            return updatedRecipe
        }

        throw GraphQLException("Could not update recipe with id $recipeId")
    }

    fun createTag(tag: TagInput): Tag? {
        val newTag = Tag(text = tag.text)
        val newTagId = tagRepository.save(newTag)
        if (newTagId != 0) {
            return newTag.copy(id = newTagId.toLong())
        }
        return null
    }

    @PreAuthorize("@authService.isRecipeEditableForUser(#user, #recipeId)")
    fun deleteRecipe(user: User, recipeId: Long): Long {
        val recipe = recipeRepository.getRecipe(userId = user.id, recipeId = recipeId) ?: throw GraphQLException("Recipe with id: $recipeId not found")

        val deletedFromRecipeList = recipeRepository.deleteRecipeFromRecipeList(recipeId = recipeId, recipeListId = recipe.recipeListId)
        if (deletedFromRecipeList > 0) {
            tagRepository.deleteTagsForRecipe(recipeId)

            val deletedRecipe = recipeRepository.deleteRecipe(recipeId)
            if (deletedRecipe > 0) {
                return recipeId
            }
        }

        throw GraphQLException("Could not delete recipe (id: $recipeId)")
    }

    @PreAuthorize("@authService.isMemberOfLocation(#user, #locationId)")
    fun getTagsForLocation(user: User, locationId: Long): List<Tag> {
        return tagRepository.getTagsForLocation(locationId)
    }
}
