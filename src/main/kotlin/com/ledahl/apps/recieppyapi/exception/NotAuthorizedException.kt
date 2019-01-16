package com.ledahl.apps.recieppyapi.exception

import org.springframework.security.access.AuthorizationServiceException

class NotAuthorizedException(override val message: String = "Not authorized for this operation"): AuthorizationServiceException(message)