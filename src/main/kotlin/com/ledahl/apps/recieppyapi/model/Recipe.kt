package com.ledahl.apps.recieppyapi.model

import java.time.LocalDateTime

data class Recipe(val id: Long = 0,
                  val recipeListId: Long,
                  val title: String,
                  val url: String?,
                  val imageUrl: String?,
                  val site: String?,
                  val comment: String?,
                  val shared: Boolean = false,
                  val created: LocalDateTime = LocalDateTime.now(),
                  val ingredients: String?,
                  val createdBy: String = "")