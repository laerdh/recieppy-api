package com.ledahl.apps.recieppyapi.resolver

import com.coxautodev.graphql.tools.GraphQLQueryResolver
import com.ledahl.apps.recieppyapi.auth.context.AuthContext
import com.ledahl.apps.recieppyapi.model.Recipe
import com.ledahl.apps.recieppyapi.model.RecipeList
import com.ledahl.apps.recieppyapi.model.Tag
import com.ledahl.apps.recieppyapi.model.User
import com.ledahl.apps.recieppyapi.service.RecipeListService
import com.ledahl.apps.recieppyapi.service.RecipeService
import com.ledahl.apps.recieppyapi.service.UserService
import graphql.schema.DataFetchingEnvironment
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Component

@Component
class Query(@Autowired private val userService: UserService,
            @Autowired private val recipeService: RecipeService,
            @Autowired private val recipeListService: RecipeListService) : GraphQLQueryResolver {

    fun getUsers(env: DataFetchingEnvironment): List<User> {
        val user = env.getContext<AuthContext>().user
        return userService.getUsers(user)
    }

    fun getUser(env: DataFetchingEnvironment): User? {
        return env.getContext<AuthContext>().user
    }

    fun getRecipe(id: Long, env: DataFetchingEnvironment): Recipe? {
        val user = env.getContext<AuthContext>().user
        return recipeService.getRecipe(id = id, user = user)
    }

    fun getRecipes(env: DataFetchingEnvironment): List<Recipe> {
        val user = env.getContext<AuthContext>().user
        return recipeService.getRecipesForUser(user)
    }

    fun getRecipeList(id: Long, env: DataFetchingEnvironment): RecipeList? {
        val user = env.getContext<AuthContext>().user
        return recipeListService.getRecipeList(id = id, user = user)
    }

    fun getRecipeLists(env: DataFetchingEnvironment): List<RecipeList> {
        val user = env.getContext<AuthContext>().user
        return recipeListService.getRecipeListsForUser(user)
    }

    fun getTags(env: DataFetchingEnvironment): List<Tag> {
        val user = env.getContext<AuthContext>().user
        return recipeService.getTags(user)
    }
}