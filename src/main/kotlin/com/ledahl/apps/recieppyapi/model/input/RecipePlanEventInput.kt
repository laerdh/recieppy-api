package com.ledahl.apps.recieppyapi.model.input

import java.time.LocalDate

data class RecipePlanEventInput(val recipeId: Long,
                                val currentDate: LocalDate,
                                val newDate: LocalDate? = null)