package com.ledahl.apps.recieppyapi.auth.model

import com.ledahl.apps.recieppyapi.model.User

data class SmsAuthResponse(val user: User?,
                           val token: String?)