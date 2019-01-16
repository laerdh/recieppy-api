package com.ledahl.apps.recieppyapi.exception

import org.springframework.security.core.AuthenticationException

class UserNotFoundException(message: String = "User not found"): AuthenticationException(message)