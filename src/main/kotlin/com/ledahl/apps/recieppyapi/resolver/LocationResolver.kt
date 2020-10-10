package com.ledahl.apps.recieppyapi.resolver

import com.coxautodev.graphql.tools.GraphQLResolver
import com.ledahl.apps.recieppyapi.auth.context.AuthContext
import com.ledahl.apps.recieppyapi.model.Location
import com.ledahl.apps.recieppyapi.model.RecipeList
import com.ledahl.apps.recieppyapi.model.RecipePlan
import com.ledahl.apps.recieppyapi.model.UserProfile
import com.ledahl.apps.recieppyapi.service.RecipeListService
import com.ledahl.apps.recieppyapi.service.RecipePlanService
import com.ledahl.apps.recieppyapi.service.UserService
import graphql.schema.DataFetchingEnvironment
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class LocationResolver(@Autowired private val recipeListService: RecipeListService,
                       @Autowired private val recipePlanService: RecipePlanService,
                       @Autowired private val userService: UserService): GraphQLResolver<Location> {
    fun getRecipeLists(location: Location, env: DataFetchingEnvironment): List<RecipeList> {
        val user = env.getContext<AuthContext>().user
        return recipeListService.getRecipeListsForUser(user = user, locationId = location.id)
    }

    fun getRecipePlan(location: Location, env: DataFetchingEnvironment): RecipePlan {
        val user = env.getContext<AuthContext>().user
        return recipePlanService.getRecipePlanForCurrentWeek(user = user, locationId = location.id)
    }

    fun getMembers(location: Location, env: DataFetchingEnvironment): List<UserProfile> {
        return userService.getUsersInLocation(location.id)
    }
}