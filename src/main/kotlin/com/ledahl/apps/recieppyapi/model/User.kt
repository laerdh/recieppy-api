package com.ledahl.apps.recieppyapi.model

import com.ledahl.apps.recieppyapi.model.enums.UserRole

data class User(val id: Long = 0,
                val firstName: String,
                val lastName: String,
                val email: String? = null,
                val firebaseId: String? = null,
                val phoneNumber: String,
                val token: String? = null,
                val role: UserRole = UserRole.USER,
                val pushToken: String? = null)