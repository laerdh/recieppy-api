package com.ledahl.apps.recieppyapi.model

data class Recipe(val id: Long = 0,
                  val title: String,
                  val url: String?,
                  val imageUrl: String?,
                  val site: String?,
                  val recipeListId: Long)