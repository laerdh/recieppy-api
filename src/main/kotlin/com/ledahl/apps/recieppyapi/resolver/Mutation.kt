package com.ledahl.apps.recieppyapi.resolver

import com.coxautodev.graphql.tools.GraphQLMutationResolver
import com.ledahl.apps.recieppyapi.auth.Unsecured
import com.ledahl.apps.recieppyapi.auth.model.AuthData
import com.ledahl.apps.recieppyapi.auth.model.AuthResponse
import com.ledahl.apps.recieppyapi.auth.context.AuthContext
import com.ledahl.apps.recieppyapi.model.Recipe
import com.ledahl.apps.recieppyapi.model.RecipeList
import com.ledahl.apps.recieppyapi.model.Tag
import com.ledahl.apps.recieppyapi.model.User
import com.ledahl.apps.recieppyapi.model.input.RecipeInput
import com.ledahl.apps.recieppyapi.model.input.RecipeListInput
import com.ledahl.apps.recieppyapi.model.input.TagInput
import com.ledahl.apps.recieppyapi.model.input.UserInput
import com.ledahl.apps.recieppyapi.service.*
import graphql.schema.DataFetchingEnvironment
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class Mutation(@Autowired private val authService: AuthService,
               @Autowired private val recipeService: RecipeService,
               @Autowired private val recipeListService: RecipeListService,
               @Autowired private val userService: UserService) : GraphQLMutationResolver {

    @Unsecured
    fun authenticate(authData: AuthData): AuthResponse {
        return authService.authenticate(authData)
    }

    fun newRecipeList(recipeList: RecipeListInput, env: DataFetchingEnvironment): RecipeList? {
        val user = env.getContext<AuthContext>().user
        return recipeListService.createRecipeList(recipeList = recipeList, user = user)
    }

    fun deleteRecipeList(id: Long, env: DataFetchingEnvironment): Long {
        val user = env.getContext<AuthContext>().user
        return recipeListService.deleteRecipeList(id = id, user = user)
    }

    fun newRecipe(recipe: RecipeInput, env: DataFetchingEnvironment): Recipe? {
        val user = env.getContext<AuthContext>().user
        return recipeService.createRecipe(recipe = recipe, user = user)
    }

    fun deleteRecipe(id: Long, env: DataFetchingEnvironment): Long? {
        val user = env.getContext<AuthContext>().user
        return recipeService.deleteRecipe(id = id, user = user)
    }

    fun newTag(tag: TagInput, env: DataFetchingEnvironment): Tag? {
        val user = env.getContext<AuthContext>().user
        return recipeService.createTag(tag = tag, user = user)
    }

    fun updateUser(user: UserInput, env: DataFetchingEnvironment): User {
        val existingUser = env.getContext<AuthContext>().user
        return userService.updateUser(updatedUser = user, user = existingUser)
    }

    fun savePushToken(pushToken: String?, env: DataFetchingEnvironment): Int? {
        val user = env.getContext<AuthContext>().user
        return userService.savePushToken(pushToken, user)
    }
}