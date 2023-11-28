package com.ledahl.apps.recieppyapi.controller

import com.ledahl.apps.recieppyapi.model.Recipe
import com.ledahl.apps.recieppyapi.model.RecipeList
import com.ledahl.apps.recieppyapi.model.User
import com.ledahl.apps.recieppyapi.model.input.RecipeListInput
import com.ledahl.apps.recieppyapi.service.RecipeListService
import com.ledahl.apps.recieppyapi.service.RecipeService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.graphql.data.method.annotation.SchemaMapping
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller

@Controller
class RecipeListController(@Autowired private val recipeListService: RecipeListService,
                           @Autowired private val recipeService: RecipeService) {
    @QueryMapping
    fun getRecipeList(@AuthenticationPrincipal user: User, @Argument id: Long): RecipeList? {
        return recipeListService.getRecipeList(user, id)
    }

    @QueryMapping
    fun getRecipeLists(@AuthenticationPrincipal user: User, @Argument locationId: Long): List<RecipeList> {
        return recipeListService.getRecipeListsForUser(user, locationId)
    }

    @MutationMapping
    fun newRecipeList(@AuthenticationPrincipal user: User, @Argument recipeList: RecipeListInput): RecipeList? {
        return recipeListService.createRecipeList(user, recipeList)
    }

    @MutationMapping
    fun deleteRecipeList(@AuthenticationPrincipal user: User, @Argument id: Long): Long {
        return recipeListService.deleteRecipeList(user, id)
    }

    @MutationMapping
    fun renameRecipeList(@AuthenticationPrincipal user: User, @Argument id: Long, @Argument newName: String): RecipeList? {
        return recipeListService.renameRecipeList(user, id, newName)
    }

    @SchemaMapping
    fun recipes(@AuthenticationPrincipal user: User, recipeList: RecipeList): List<Recipe> {
        return recipeService.getRecipesForRecipeList(user.id, recipeList)
    }
}