package com.ledahl.apps.recieppyapi.auth.model

import com.ledahl.apps.recieppyapi.model.User

data class AuthResponse(val user: User,
                        val firstLogin: Boolean = false)