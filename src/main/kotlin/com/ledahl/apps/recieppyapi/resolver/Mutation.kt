package com.ledahl.apps.recieppyapi.resolver

import com.coxautodev.graphql.tools.GraphQLMutationResolver
import com.ledahl.apps.recieppyapi.auth.context.AuthContext
import com.ledahl.apps.recieppyapi.model.*
import com.ledahl.apps.recieppyapi.model.input.*
import com.ledahl.apps.recieppyapi.service.*
import graphql.schema.DataFetchingEnvironment
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class Mutation(@Autowired private val recipeService: RecipeService,
               @Autowired private val recipeListService: RecipeListService,
               @Autowired private val userService: UserService,
               @Autowired private val locationService: LocationService,
               @Autowired private val recipePlanService: RecipePlanService)
    : GraphQLMutationResolver {

    fun newRecipeList(recipeList: RecipeListInput, env: DataFetchingEnvironment): RecipeList? {
        val user = env.getContext<AuthContext>().user
        return recipeListService.createRecipeList(user = user, recipeList = recipeList)
    }

    fun deleteRecipeList(id: Long, env: DataFetchingEnvironment): Long {
        val user = env.getContext<AuthContext>().user
        return recipeListService.deleteRecipeList(user = user, recipeListId = id)
    }

    fun renameRecipeList(id: Long, newName: String, env: DataFetchingEnvironment): RecipeList? {
        val user = env.getContext<AuthContext>().user
        return recipeListService.renameRecipeList(user = user, recipeListId = id, newName = newName)
    }

    fun newRecipe(recipeInput: RecipeInput, env: DataFetchingEnvironment): Recipe? {
        val user = env.getContext<AuthContext>().user
        return recipeService.createRecipe(user = user, recipeInput = recipeInput)
    }

    fun updateRecipe(id: Long, recipeInput: RecipeInput, env: DataFetchingEnvironment): Recipe? {
        val user = env.getContext<AuthContext>().user
        return recipeService.updateRecipe(
                user = user,
                recipeId = id,
                recipeInput = recipeInput
        )
    }

    fun deleteRecipe(recipeId: Long, env: DataFetchingEnvironment): Long? {
        val user = env.getContext<AuthContext>().user
        return recipeService.deleteRecipe(user = user, recipeId = recipeId)
    }

    fun newTag(tag: TagInput): Tag? {
        return recipeService.createTag(tag = tag)
    }

    fun savePushToken(pushToken: String?, env: DataFetchingEnvironment): Int? {
        val user = env.getContext<AuthContext>().user
        return userService.savePushToken(pushToken, user)
    }

    fun newLocation(input: NewLocationInput, env: DataFetchingEnvironment): Location? {
        val user = env.getContext<AuthContext>().user
        return locationService.createNewLocation(input, user)
    }

    fun updateLocation(locationId: Long, input: NewLocationInput, env: DataFetchingEnvironment): Location? {
        val user = env.getContext<AuthContext>().user
        return locationService.updateLocation(locationId, input, user)
    }

    fun removeCurrentUserFromLocation(locationId: Long, env: DataFetchingEnvironment): List<Location> {
        val user = env.getContext<AuthContext>().user
        return locationService.removeCurrentUserFromLocation(user, locationId)
    }

    fun removeUserFromLocation(userId: Long, locationId: Long, env: DataFetchingEnvironment): Location? {
        val user = env.getContext<AuthContext>().user
        return locationService.removeUserFromLocation(user, userId, locationId)
    }

    fun acceptInvite(inviteCode: String, env: DataFetchingEnvironment): Boolean {
        val user = env.getContext<AuthContext>().user
        return locationService.acceptInviteForUser(user, inviteCode)
    }

    fun newRecipePlanEvent(locationId: Long, recipePlanEvent: RecipePlanEventInput, env: DataFetchingEnvironment): RecipePlan {
        val user = env.getContext<AuthContext>().user
        return recipePlanService.createRecipePlanEvent(
                user = user,
                locationId = locationId,
                recipePlanEvent = recipePlanEvent
        )
    }

    fun updateRecipePlanEvent(locationId: Long, recipePlanEvent: RecipePlanEventInput, env: DataFetchingEnvironment): RecipePlan {
        val user = env.getContext<AuthContext>().user
        return recipePlanService.updateRecipePlanEvent(
                user = user,
                locationId = locationId,
                recipePlanEvent = recipePlanEvent
        )
    }

    fun deleteRecipePlanEvent(locationId: Long, recipePlanEvent: RecipePlanEventInput, env: DataFetchingEnvironment): RecipePlan {
        val user = env.getContext<AuthContext>().user
        return recipePlanService.deleteRecipePlanEvent(
                user = user,
                locationId = locationId,
                recipePlanEvent = recipePlanEvent
        )
    }
}