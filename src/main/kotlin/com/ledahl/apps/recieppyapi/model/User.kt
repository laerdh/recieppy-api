package com.ledahl.apps.recieppyapi.model

import com.ledahl.apps.recieppyapi.model.enums.UserRole

data class User(val id: Long = 0,
                val subject: String,
                val firstName: String,
                val lastName: String,
                val email: String? = null,
                val phoneNumber: String,
                val role: UserRole = UserRole.USER,
                val pushToken: String? = null,
                val firstLogin: Boolean = false)