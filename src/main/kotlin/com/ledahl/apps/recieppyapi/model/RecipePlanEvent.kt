package com.ledahl.apps.recieppyapi.model

import java.time.LocalDate

data class RecipePlanEvent(val recipeId: Long,
                           val date: LocalDate)