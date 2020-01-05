package com.ledahl.apps.recieppyapi.service

import com.ledahl.apps.recieppyapi.exception.NotAuthorizedException
import com.ledahl.apps.recieppyapi.model.Recipe
import com.ledahl.apps.recieppyapi.model.RecipeList
import com.ledahl.apps.recieppyapi.model.Tag
import com.ledahl.apps.recieppyapi.model.User
import com.ledahl.apps.recieppyapi.model.input.RecipeInput
import com.ledahl.apps.recieppyapi.model.input.TagInput
import com.ledahl.apps.recieppyapi.repository.LocationRepository
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
                    @Autowired private val tagRepository: TagRepository,
                    @Autowired private val locationRepository: LocationRepository) {

    private val logger = LoggerFactory.getLogger(RecipeService::class.java)

    @PreAuthorize("@authService.isRecipeInUsersLocation(#user, #recipeId)")
    fun getRecipe(user: User, recipeId: Long): Recipe? {
        return recipeRepository.getRecipe(recipeId)
    }

    fun getRecipeForRecipePlan(recipeId: Long): Recipe? {
        return recipeRepository.getRecipe(recipeId)
    }

    @PreAuthorize("@authService.isMemberOfLocation(#user, #locationId)")
    fun getRecipesForLocation(user: User, locationId: Long): List<Recipe> {
        return recipeRepository.getRecipesForLocation(locationId = locationId)
    }

    fun getRecipesForRecipeList(recipeList: RecipeList): List<Recipe> {
        return recipeRepository.getRecipesForRecipeList(recipeList.id)
    }

    fun getTags(): List<Tag> {
        return tagRepository.getTags()
    }

    fun getTagsForRecipe(recipeId: Long): List<Tag> {
        return tagRepository.getTagsForRecipe(recipeId)
    }

    @PreAuthorize("@authService.isRecipeListInUsersLocation(#user, #recipeInput.recipeListId)")
    fun createRecipe(user: User, recipeInput: RecipeInput): Recipe? {
        val locationId = locationRepository.getLocationId(user.id, recipeInput.recipeListId)

        val noRecipeLists = recipeListRepository
                .getRecipeLists(user.id, locationId)
                .none { it.id == recipeInput.recipeListId }

        if (noRecipeLists) {
            throw NotAuthorizedException("User does not subscribe to this recipelist")
        }

        val newRecipe = Recipe(
                title = recipeInput.title,
                url = recipeInput.url,
                imageUrl = recipeInput.imageUrl,
                site = recipeInput.site,
                comment = recipeInput.comment,
                recipeListId = recipeInput.recipeListId
        )

        val newRecipeId = recipeRepository.save(newRecipe)
        if (newRecipeId != 0) {
            recipeInput.tags?.let {
                recipeRepository.saveTagsToRecipe(recipeId = newRecipeId.toLong(), tags = it)
            }

            return newRecipe.copy(id = newRecipeId.toLong())
        }
        return null
    }

    fun createTag(tag: TagInput): Tag? {
        val newTag = Tag(text = tag.text)
        val newTagId = tagRepository.save(newTag)
        if (newTagId != 0) {
            return newTag.copy(id = newTagId.toLong())
        }
        return null
    }

    @PreAuthorize("@authService.isRecipeInUsersLocation(#user, #recipeId)")
    fun deleteRecipe(user: User, recipeId: Long): Long {
        val recipe = recipeRepository.getRecipe(recipeId) ?: throw GraphQLException("Recipe with id: $recipeId not found")
        val locationId = locationRepository.getLocationId(user.id, recipe.recipeListId)
        val usersRecipeLists = recipeListRepository.getRecipeLists(user.id, locationId)

        val userIsOwner = usersRecipeLists.any { it.id == recipe.recipeListId }
        if (!userIsOwner) {
            logger.info("User (id: {}) tried to delete other users recipe (id: {})", user.id, recipeId)
            throw NotAuthorizedException("User is not owner of recipe list (id: $recipeId)")
        }

        val deleted = recipeRepository.delete(recipeId)
        if (deleted == 0) {
            throw GraphQLException("Recipe (id: $recipeId) not found")
        }

        return recipeId
    }
}