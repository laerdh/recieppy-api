package com.ledahl.apps.recieppyapi.controller

import com.ledahl.apps.recieppyapi.model.Recipe
import com.ledahl.apps.recieppyapi.model.Tag
import com.ledahl.apps.recieppyapi.model.User
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
    fun recipe(@AuthenticationPrincipal user: User, @Argument recipeId: Long): Recipe? {
        return recipeService.getRecipe(user, recipeId)
    }

    @QueryMapping
    fun recipes(@AuthenticationPrincipal user: User, @Argument locationId: Long): List<Recipe> {
        return recipeService.getRecipesForUser(user, locationId)
    }

    @QueryMapping
    fun sharedRecipes(@AuthenticationPrincipal user: User): List<Recipe> {
        return recipeService.getSharedRecipes(user.id)
    }

    @MutationMapping
    fun newRecipe(@AuthenticationPrincipal user: User, @Argument recipeInput: RecipeInput): Recipe? {
        return recipeService.createRecipe(user, recipeInput)
    }

    @MutationMapping
    fun updateRecipe(@AuthenticationPrincipal user: User, @Argument id: Long, @Argument recipeInput: RecipeInput): Recipe? {
        return recipeService.updateRecipe(
            user = user,
            recipeId = id,
            recipeInput = recipeInput
        )
    }

    @MutationMapping
    fun deleteRecipe(@AuthenticationPrincipal user: User, @Argument recipeId: Long): Long? {
        return recipeService.deleteRecipe(user, recipeId)
    }

    @SchemaMapping
    fun tags(recipe: Recipe): List<Tag>? {
        return recipeService.getTagsForRecipe(recipe.id)
    }
}