package com.ledahl.apps.recieppyapi.exception

import org.springframework.security.core.AuthenticationException

class NotAuthorizedException(override val message: String = "User not authorized for this operation"): AuthenticationException(message)