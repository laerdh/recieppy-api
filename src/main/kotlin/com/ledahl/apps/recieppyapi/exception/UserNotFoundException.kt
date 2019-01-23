package com.ledahl.apps.recieppyapi.exception

import java.lang.RuntimeException

class UserNotFoundException(message: String = "User not found"): RuntimeException(message)