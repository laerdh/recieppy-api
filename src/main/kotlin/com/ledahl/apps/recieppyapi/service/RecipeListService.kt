package com.ledahl.apps.recieppyapi.service

import com.ledahl.apps.recieppyapi.exception.NotAuthorizedException
import com.ledahl.apps.recieppyapi.model.RecipeList
import com.ledahl.apps.recieppyapi.model.User
import com.ledahl.apps.recieppyapi.model.input.RecipeListInput
import com.ledahl.apps.recieppyapi.repository.LocationRepository
import com.ledahl.apps.recieppyapi.repository.RecipeListRepository
import com.ledahl.apps.recieppyapi.repository.RecipeRepository
import graphql.GraphQLException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class RecipeListService(@Autowired private val recipeListRepository: RecipeListRepository,
                        @Autowired private val recipeRepository: RecipeRepository,
                        @Autowired private val locationRepository: LocationRepository) {
    fun getRecipeList(id: Long, user: User?): RecipeList? {
        user ?: throw NotAuthorizedException()
        return recipeListRepository.getRecipeList(id = id, userId = user.id)
    }

    fun getRecipeListsForUser(user: User?, locationId: Int): List<RecipeList> {
        user ?: throw NotAuthorizedException()
        return recipeListRepository.getRecipeLists(user.id, locationId)
    }

    fun createRecipeList(recipeList: RecipeListInput, user: User?): RecipeList? {
        user ?: throw NotAuthorizedException()

        val newRecipeList = RecipeList(name = recipeList.name, created = LocalDate.now())
        val newRecipeListId = recipeListRepository.save(newRecipeList)

        if (newRecipeListId != 0) {

            recipeListRepository.connectRecipeListAndLocation(
                    recipeListId = newRecipeListId.toLong(),
                    locationId = recipeList.locationId.toLong())

            return newRecipeList.copy(id = newRecipeListId.toLong())
        }
        return null
    }

    fun deleteRecipeList(recipeListId: Long, user: User?): Long {
        user ?: throw NotAuthorizedException()

        val locationId = locationRepository.getLocationId(user.id, recipeListId)

        if (locationId == null) {
            throw GraphQLException("Could not delete recipelist: No location found for provided recipeListId and userId")
        }

        val locationRecipeListDeleted = recipeListRepository.deleteLocationRecipeList(
                recipeListId = recipeListId,
                locationId = locationId)

        if (locationRecipeListDeleted == 0) {
            throw GraphQLException("Recipe list (id: $recipeListId) not found")
        }

        recipeRepository.deleteRecipesForRecipeList(recipeListId = recipeListId)

        recipeListRepository.deleteRecipeList(recipeListId = recipeListId)
        return recipeListId
    }

    fun renameRecipeList(user: User?, recipeListId: Long, newName: String): RecipeList? {
        user ?: throw NotAuthorizedException()

        val recipeListForUser = getRecipeList(recipeListId, user)

        if (recipeListForUser == null) {
            throw GraphQLException("Recipe list (id: $recipeListId) not found")
        }

        val updated = recipeListRepository.renameRecipeList(
                recipeListId = recipeListId,
                newName = newName)

        if (updated == 0) {
            throw GraphQLException("Recipe list (id: $recipeListId) not found")
        }

        return getRecipeList(recipeListId, user)
    }
}