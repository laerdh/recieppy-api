package com.ledahl.apps.recieppyapi.service

import com.ledahl.apps.recieppyapi.exception.NotAuthorizedException
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
import org.springframework.stereotype.Service

@Service
class RecipeService(@Autowired private val recipeRepository: RecipeRepository,
                    @Autowired private val recipeListRepository: RecipeListRepository,
                    @Autowired private val tagRepository: TagRepository) {
    private val logger = LoggerFactory.getLogger(RecipeService::class.java)

    fun getRecipe(id: Long, user: User?): Recipe? {
        user ?: throw NotAuthorizedException()
        return recipeRepository.getRecipe(id)
    }

    fun getRecipesForUser(user: User?): List<Recipe> {
        user ?: throw NotAuthorizedException()
        return recipeRepository.getRecipesForUser(user.id)
    }

    fun getRecipesForRecipeList(recipeList: RecipeList): List<Recipe> {
        return recipeRepository.getRecipesForRecipeList(recipeList.id)
    }

    fun getTags(user: User?): List<Tag> {
        user ?: throw NotAuthorizedException()
        return tagRepository.getTags()
    }

    fun getTagsForRecipe(recipeId: Long): List<Tag> {
        return tagRepository.getTagsForRecipe(recipeId)
    }

    fun createRecipe(recipe: RecipeInput, user: User?): Recipe? {
        user ?: throw NotAuthorizedException()
        val noRecipeLists = recipeListRepository.getRecipeLists(user.id).none { it.id == recipe.recipeListId }
        if (noRecipeLists) {
            throw NotAuthorizedException("User does not subscribe to this recipelist")
        }

        val newRecipe = Recipe(
                title = recipe.title,
                url = recipe.url,
                imageUrl = recipe.imageUrl,
                site = recipe.site,
                recipeListId = recipe.recipeListId
        )

        val newRecipeId = recipeRepository.save(newRecipe)
        if (newRecipeId != 0) {
            recipe.tags?.let {
                recipeRepository.saveTagsToRecipe(recipeId = newRecipeId.toLong(), tags = it)
            }

            return newRecipe.copy(id = newRecipeId.toLong())
        }
        return null
    }

    fun createTag(tag: TagInput, user: User?): Tag? {
        user ?: throw NotAuthorizedException()

        val newTag = Tag(text = tag.text)
        val newTagId = tagRepository.save(newTag)
        if (newTagId != 0) {
            return newTag.copy(id = newTagId.toLong())
        }
        return null
    }

    fun deleteRecipe(id: Long, user: User?): Long {
        user ?: throw NotAuthorizedException()

        val recipe = recipeRepository.getRecipe(id) ?: throw GraphQLException("Recipe with id: $id not found")
        val usersRecipeLists = recipeListRepository.getRecipeLists(user.id)
        val userIsOwner = usersRecipeLists.any { it.id == recipe.recipeListId }
        if (!userIsOwner) {
            logger.info("User (id: {}) tried to delete other users recipe (id: {})", user.id, id)
            throw NotAuthorizedException("User is not owner of recipe list (id: $id)")
        }

        val deleted = recipeRepository.delete(id)
        if (deleted == 0) {
            throw GraphQLException("Recipe (id: $id) not found")
        }

        return id
    }
}