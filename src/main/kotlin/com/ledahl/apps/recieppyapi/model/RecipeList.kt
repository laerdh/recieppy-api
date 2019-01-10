package com.ledahl.apps.recieppyapi.model

import java.time.LocalDate

data class RecipeList(val id: Long = 0L,
                      val name: String,
                      val created: LocalDate = LocalDate.now())