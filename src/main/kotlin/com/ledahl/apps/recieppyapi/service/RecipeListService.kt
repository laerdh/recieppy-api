package com.ledahl.apps.recieppyapi.service

import com.ledahl.apps.recieppyapi.model.RecipeList
import com.ledahl.apps.recieppyapi.model.User
import com.ledahl.apps.recieppyapi.model.input.RecipeListInput
import com.ledahl.apps.recieppyapi.repository.LocationRepository
import com.ledahl.apps.recieppyapi.repository.RecipeListRepository
import com.ledahl.apps.recieppyapi.repository.RecipeRepository
import graphql.GraphQLException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service

@Service
class RecipeListService(@Autowired private val recipeListRepository: RecipeListRepository,
                        @Autowired private val recipeRepository: RecipeRepository,
                        @Autowired private val locationRepository: LocationRepository) {

    @PreAuthorize("@authService.isRecipeListAvailableToUser(#user, #recipeListId)")
    fun getRecipeList(user: User, recipeListId: Long): RecipeList? {
        return recipeListRepository.getRecipeList(userId = user.id, recipeListId = recipeListId)
    }

    @PreAuthorize("@authService.isMemberOfLocation(#user, #locationId)")
    fun getRecipeListsForUser(user: User, locationId: Long): List<RecipeList> {
        return recipeListRepository.getRecipeLists(user.id, locationId)
    }

    @PreAuthorize("@authService.isMemberOfLocation(#user, #recipeList.locationId)")
    fun createRecipeList(user: User, recipeList: RecipeListInput): RecipeList? {
        val newRecipeList = RecipeList(name = recipeList.name)
        val newRecipeListId = recipeListRepository.createRecipeList(userId = user.id, recipeList = newRecipeList)

        if (newRecipeListId != 0) {
            recipeListRepository.connectRecipeListAndLocation(
                    recipeListId = newRecipeListId.toLong(),
                    locationId = recipeList.locationId.toLong())

            return newRecipeList.copy(id = newRecipeListId.toLong())
        }
        return null
    }

    @PreAuthorize("@authService.isRecipeListEditableForUser(#user, #recipeListId)")
    fun deleteRecipeList(user: User, recipeListId: Long): Long {
        val locationId = locationRepository.getLocationId(user.id, recipeListId)
        val locationRecipeListDeleted = recipeListRepository.deleteLocationRecipeList(
                recipeListId = recipeListId,
                locationId = locationId)

        if (locationRecipeListDeleted == 0) {
            throw GraphQLException("Recipe list (id: $recipeListId) not found")
        }

        recipeRepository.getRecipesForRecipeList(userId = user.id, recipeListId = recipeListId).forEach {
            recipeRepository.deleteRecipeFromRecipeList(recipeId = it.id, recipeListId = recipeListId)
        }
        recipeRepository.deleteRecipesForRecipeList(recipeListId = recipeListId)

        recipeListRepository.deleteRecipeList(recipeListId = recipeListId)
        return recipeListId
    }

    @PreAuthorize("@authService.isRecipeListEditableForUser(#user, #recipeListId)")
    fun renameRecipeList(user: User, recipeListId: Long, newName: String): RecipeList? {
        val updated = recipeListRepository.renameRecipeList(
                recipeListId = recipeListId,
                newName = newName
        )

        if (updated == 0) {
            throw GraphQLException("Recipe list (id: $recipeListId) not found")
        }

        return getRecipeList(user, recipeListId)
    }
}