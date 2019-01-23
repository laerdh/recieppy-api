package com.ledahl.apps.recieppyapi.resolver

import com.coxautodev.graphql.tools.GraphQLResolver
import com.ledahl.apps.recieppyapi.auth.model.AuthResponse
import com.ledahl.apps.recieppyapi.model.RecipeList
import com.ledahl.apps.recieppyapi.model.User
import com.ledahl.apps.recieppyapi.service.RecipeListService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class UserResolver(@Autowired private val recipeListService: RecipeListService): GraphQLResolver<User> {
    fun getUser(authResponse: AuthResponse): User {
        return authResponse.user
    }

    fun getRecipeLists(user: User): List<RecipeList> {
        return recipeListService.getRecipeListsForUser(user)
    }
}