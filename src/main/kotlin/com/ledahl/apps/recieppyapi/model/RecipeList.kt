package com.ledahl.apps.recieppyapi.model

import java.time.LocalDateTime

data class RecipeList(val id: Long = 0,
                      val name: String,
                      val shared: Boolean = false,
                      val created: LocalDateTime = LocalDateTime.now(),
                      val createdBy: String = "N/A")