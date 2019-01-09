package com.ledahl.apps.recieppyapi.config

import com.ledahl.apps.recieppyapi.repository.RecipeRepository
import com.ledahl.apps.recieppyapi.repository.TagRepository
import com.ledahl.apps.recieppyapi.resolver.Query
import com.ledahl.apps.recieppyapi.resolver.RecipeResolver
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class GraphqlConfiguration {
    @Bean
    fun recipeResolver(tagRepository: TagRepository): RecipeResolver {
        return RecipeResolver(tagRepository = tagRepository)
    }

    @Bean
    fun query(recipeRepository: RecipeRepository,
              tagRepository: TagRepository): Query {
        return Query(
                recipeRepository = recipeRepository,
                tagRepository = tagRepository
        )
    }
}