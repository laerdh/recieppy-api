package com.ledahl.apps.recieppyapi.model.input

import java.time.LocalDate

data class RecipeEventInput(val recipeId: Long,
                            val date: LocalDate)