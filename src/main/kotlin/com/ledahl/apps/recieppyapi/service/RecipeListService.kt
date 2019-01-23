package com.ledahl.apps.recieppyapi.service

import com.ledahl.apps.recieppyapi.exception.NotAuthorizedException
import com.ledahl.apps.recieppyapi.model.RecipeList
import com.ledahl.apps.recieppyapi.model.User
import com.ledahl.apps.recieppyapi.model.input.RecipeListInput
import com.ledahl.apps.recieppyapi.repository.RecipeListRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class RecipeListService(@Autowired private val recipeListRepository: RecipeListRepository) {
    fun getRecipeList(id: Long, user: User?): RecipeList? {
        user ?: throw NotAuthorizedException()
        return recipeListRepository.getRecipeList(id = id, userId = user.id)
    }

    fun getRecipeListsForUser(user: User?): List<RecipeList> {
        user ?: throw NotAuthorizedException()
        return recipeListRepository.getRecipeLists(user.id)
    }

    fun createRecipeList(recipeList: RecipeListInput, user: User?): RecipeList? {
        user ?: throw NotAuthorizedException()
        val newRecipeList = RecipeList(name = recipeList.name, created = LocalDate.now())

        val newRecipeListId = recipeListRepository.save(newRecipeList)
        if (newRecipeListId != 0) {
            recipeListRepository.saveRecipeList(recipeListId = newRecipeListId.toLong(), userId = user.id)
            return newRecipeList.copy(id = newRecipeListId.toLong())
        }
        return null
    }
}