package com.ledahl.apps.recieppyapi.model

import java.time.LocalDate

data class RecipeList(val id: Long = 0,
                      val name: String,
                      val created: LocalDate)