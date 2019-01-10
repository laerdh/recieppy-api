package com.ledahl.apps.recieppyapi.config

import com.ledahl.apps.recieppyapi.repository.RecipeListRepository
import com.ledahl.apps.recieppyapi.repository.RecipeRepository
import com.ledahl.apps.recieppyapi.repository.TagRepository
import com.ledahl.apps.recieppyapi.resolver.Mutation
import com.ledahl.apps.recieppyapi.resolver.Query
import com.ledahl.apps.recieppyapi.resolver.RecipeListResolver
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
    fun recipeListResolver(recipeRepository: RecipeRepository): RecipeListResolver {
        return RecipeListResolver(recipeRepository = recipeRepository)
    }

    @Bean
    fun query(recipeRepository: RecipeRepository,
              recipeListRepository: RecipeListRepository,
              tagRepository: TagRepository): Query {
        return Query(
                recipeRepository = recipeRepository,
                recipeListRepository = recipeListRepository,
                tagRepository = tagRepository
        )
    }

    @Bean
    fun mutation(recipeRepository: RecipeRepository,
                 recipeListRepository: RecipeListRepository,
                 tagRepository: TagRepository): Mutation {
        return Mutation(
                recipeRepository = recipeRepository,
                recipeListRepository = recipeListRepository,
                tagRepository = tagRepository
        )
    }
}