package com.ledahl.apps.recieppyapi.controller

import com.ledahl.apps.recieppyapi.auth.JwtPrincipal
import com.ledahl.apps.recieppyapi.model.Recipe
import com.ledahl.apps.recieppyapi.model.RecipeList
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
    fun recipeList(@AuthenticationPrincipal jwtPrincipal: JwtPrincipal, @Argument id: Long): RecipeList? {
        return recipeListService.getRecipeList(jwtPrincipal.user, id)
    }

    @QueryMapping
    fun recipeLists(@AuthenticationPrincipal jwtPrincipal: JwtPrincipal, @Argument locationId: Long): List<RecipeList> {
        return recipeListService.getRecipeListsForUser(jwtPrincipal.user, locationId)
    }

    @MutationMapping
    fun newRecipeList(@AuthenticationPrincipal jwtPrincipal: JwtPrincipal, @Argument recipeList: RecipeListInput): RecipeList? {
        return recipeListService.createRecipeList(jwtPrincipal.user, recipeList)
    }

    @MutationMapping
    fun deleteRecipeList(@AuthenticationPrincipal jwtPrincipal: JwtPrincipal, @Argument id: Long): Long {
        return recipeListService.deleteRecipeList(jwtPrincipal.user, id)
    }

    @MutationMapping
    fun renameRecipeList(@AuthenticationPrincipal jwtPrincipal: JwtPrincipal, @Argument id: Long, @Argument newName: String): RecipeList? {
        return recipeListService.renameRecipeList(jwtPrincipal.user, id, newName)
    }

    @SchemaMapping
    fun recipes(@AuthenticationPrincipal jwtPrincipal: JwtPrincipal, recipeList: RecipeList): List<Recipe> {
        return recipeService.getRecipesForRecipeList(jwtPrincipal.user.id, recipeList)
    }
}