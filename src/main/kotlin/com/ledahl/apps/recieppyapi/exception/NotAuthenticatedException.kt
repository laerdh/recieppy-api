package com.ledahl.apps.recieppyapi.exception

import org.springframework.security.core.AuthenticationException

class NotAuthenticatedException(override val message: String = "User not authenticated"): AuthenticationException(message)