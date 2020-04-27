package com.ledahl.apps.recieppyapi.resolver

import com.coxautodev.graphql.tools.GraphQLQueryResolver
import com.ledahl.apps.recieppyapi.auth.context.AuthContext
import com.ledahl.apps.recieppyapi.model.*
import com.ledahl.apps.recieppyapi.service.*
import graphql.schema.DataFetchingEnvironment
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class Query(@Autowired private val userService: UserService,
            @Autowired private val recipeService: RecipeService,
            @Autowired private val recipeListService: RecipeListService,
            @Autowired private val recipePlanService: RecipePlanService,
            @Autowired private val locationService: LocationService) : GraphQLQueryResolver {

    fun getUsers(env: DataFetchingEnvironment): List<User> {
        val user = env.getContext<AuthContext>().user
        return userService.getUsers(user)
    }

    fun getUser(env: DataFetchingEnvironment): User? {
        return env.getContext<AuthContext>().user
    }

    fun getRecipe(recipeId: Long, env: DataFetchingEnvironment): Recipe? {
        val user = env.getContext<AuthContext>().user
        return recipeService.getRecipe(user = user, recipeId = recipeId)
    }

    fun getRecipes(locationId: Long, env: DataFetchingEnvironment): List<Recipe> {
        val user = env.getContext<AuthContext>().user
        return recipeService.getRecipesForLocation(user = user, locationId = locationId)
    }

    fun getRecipeList(id: Long, env: DataFetchingEnvironment): RecipeList? {
        val user = env.getContext<AuthContext>().user
        return recipeListService.getRecipeList(user = user, id = id)
    }

    fun getRecipeLists(locationId: Long, env: DataFetchingEnvironment): List<RecipeList> {
        val user = env.getContext<AuthContext>().user
        return recipeListService.getRecipeListsForUser(user, locationId)
    }

    fun getTags(env: DataFetchingEnvironment): List<Tag> {
        return recipeService.getTags()
    }

    fun getLocations(env: DataFetchingEnvironment): List<Location> {
        val user = env.getContext<AuthContext>().user
        return locationService.getLocations(user)
    }

    fun getRecipePlan(locationId: Long, weekNumber: Int, env: DataFetchingEnvironment): RecipePlan {
        val user = env.getContext<AuthContext>().user
        return recipePlanService.getRecipePlanForWeek(
                user = user,
                locationId = locationId,
                weekNumber = weekNumber
        )
    }

    fun getLocationForInviteCode(inviteCode: String): String? {
        return locationService.getLocationNameForInviteCode(inviteCode)
    }
}