package com.ledahl.apps.recieppyapi.resolver

import com.coxautodev.graphql.tools.GraphQLResolver
import com.ledahl.apps.recieppyapi.auth.context.AuthContext
import com.ledahl.apps.recieppyapi.model.Location
import com.ledahl.apps.recieppyapi.model.RecipeList
import com.ledahl.apps.recieppyapi.model.User
import com.ledahl.apps.recieppyapi.service.RecipeListService
import com.ledahl.apps.recieppyapi.service.UserService
import graphql.GraphQLException
import graphql.schema.DataFetchingEnvironment
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class LocationResolver(
        @Autowired private val recipeListService: RecipeListService
): GraphQLResolver<Location> {
    fun getRecipeLists(location: Location, env: DataFetchingEnvironment): List<RecipeList> {
        val user = env.getContext<AuthContext>().user

        return recipeListService.getRecipeListsForUser(user, location.id.toInt())
    }
}