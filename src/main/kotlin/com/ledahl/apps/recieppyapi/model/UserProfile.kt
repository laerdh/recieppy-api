package com.ledahl.apps.recieppyapi.model

data class UserProfile(val id: Long,
                       val firstName: String,
                       val lastName: String,
                       val email: String? = null)