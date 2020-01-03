package com.ledahl.apps.recieppyapi.model

import java.util.*

data class RecipePlan(val locationId: Long,
                      val weekNumber: Int = Calendar.getInstance().get(Calendar.WEEK_OF_YEAR))