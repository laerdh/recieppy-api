package com.ledahl.apps.recieppyapi.model.input

data class RecipeInput(val title: String,
                       val url: String?,
                       val imageUrl: String?,
                       val site: String?,
                       val recipeListId: Long,
                       val tags: List<Long>?,
                       val ingredients: String?,
                       val comment: String?)