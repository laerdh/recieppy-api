package com.ledahl.apps.recieppyapi.exception

import java.lang.RuntimeException

class NotAuthenticatedException(override val message: String = "User not authenticated"): RuntimeException(message)