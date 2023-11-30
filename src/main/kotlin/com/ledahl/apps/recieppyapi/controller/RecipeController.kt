package com.ledahl.apps.recieppyapi.controller

import com.ledahl.apps.recieppyapi.auth.JwtPrincipal
import com.ledahl.apps.recieppyapi.model.Recipe
import com.ledahl.apps.recieppyapi.model.Tag
import com.ledahl.apps.recieppyapi.model.input.RecipeInput
import com.ledahl.apps.recieppyapi.service.RecipeService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.graphql.data.method.annotation.SchemaMapping
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller

@Controller
class RecipeController(@Autowired private val recipeService: RecipeService) {
    @QueryMapping
    fun recipe(@AuthenticationPrincipal jwtPrincipal: JwtPrincipal, @Argument recipeId: Long): Recipe? {
        return recipeService.getRecipe(jwtPrincipal.user, recipeId)
    }

    @QueryMapping
    fun recipes(@AuthenticationPrincipal jwtPrincipal: JwtPrincipal, @Argument locationId: Long): List<Recipe> {
        return recipeService.getRecipesForUser(jwtPrincipal.user, locationId)
    }

    @QueryMapping
    fun sharedRecipes(@AuthenticationPrincipal jwtPrincipal: JwtPrincipal): List<Recipe> {
        return recipeService.getSharedRecipes(jwtPrincipal.user.id)
    }

    @MutationMapping
    fun newRecipe(@AuthenticationPrincipal jwtPrincipal: JwtPrincipal, @Argument recipeInput: RecipeInput): Recipe? {
        return recipeService.createRecipe(jwtPrincipal.user, recipeInput)
    }

    @MutationMapping
    fun updateRecipe(@AuthenticationPrincipal jwtPrincipal: JwtPrincipal, @Argument id: Long, @Argument recipeInput: RecipeInput): Recipe? {
        return recipeService.updateRecipe(
            user = jwtPrincipal.user,
            recipeId = id,
            recipeInput = recipeInput
        )
    }

    @MutationMapping
    fun deleteRecipe(@AuthenticationPrincipal jwtPrincipal: JwtPrincipal, @Argument recipeId: Long): Long? {
        return recipeService.deleteRecipe(jwtPrincipal.user, recipeId)
    }

    @SchemaMapping
    fun tags(recipe: Recipe): List<Tag>? {
        return recipeService.getTagsForRecipe(recipe.id)
    }
}