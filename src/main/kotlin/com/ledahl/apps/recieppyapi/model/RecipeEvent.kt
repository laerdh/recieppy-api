package com.ledahl.apps.recieppyapi.model

import java.time.LocalDate

data class RecipeEvent(val recipeId: Long,
                       val date: LocalDate)