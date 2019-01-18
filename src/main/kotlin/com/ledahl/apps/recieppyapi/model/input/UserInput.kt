package com.ledahl.apps.recieppyapi.model.input

data class UserInput(val firstName: String,
                     val lastName: String,
                     val email: String? = null)