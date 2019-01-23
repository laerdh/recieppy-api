package com.ledahl.apps.recieppyapi.exception

import java.lang.RuntimeException

class NotAuthorizedException(override val message: String = "User not authorized for this operation"): RuntimeException(message)