package com.ledahl.apps.recieppyapi.controller

import com.ledahl.apps.recieppyapi.auth.JwtPrincipal
import com.ledahl.apps.recieppyapi.model.Tag
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
    fun tags(): List<Tag> {
        return recipeService.getTags()
    }

    @QueryMapping
    fun tagsForLocation(@AuthenticationPrincipal jwtPrincipal: JwtPrincipal, @Argument locationId: Long): List<Tag> {
        return recipeService.getTagsForLocation(jwtPrincipal.user, locationId)
    }

    @MutationMapping
    fun newTag(@Argument tag: TagInput): Tag? {
        return recipeService.createTag(tag)
    }
}