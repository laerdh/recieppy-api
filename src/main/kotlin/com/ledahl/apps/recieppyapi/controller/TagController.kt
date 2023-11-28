package com.ledahl.apps.recieppyapi.controller

import com.ledahl.apps.recieppyapi.model.Tag
import com.ledahl.apps.recieppyapi.model.User
import com.ledahl.apps.recieppyapi.model.input.TagInput
import com.ledahl.apps.recieppyapi.service.RecipeService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller

@Controller
class TagController(@Autowired private val recipeService: RecipeService) {
    @QueryMapping
    fun getTags(): List<Tag> {
        return recipeService.getTags()
    }

    @QueryMapping
    fun getTagsForLocation(@AuthenticationPrincipal user: User, @Argument locationId: Long): List<Tag> {
        return recipeService.getTagsForLocation(user, locationId)
    }

    @MutationMapping
    fun newTag(@Argument tag: TagInput): Tag? {
        return recipeService.createTag(tag)
    }
}