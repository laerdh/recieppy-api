package com.ledahl.apps.recieppyapi.exception

import org.springframework.security.core.AuthenticationException

class NotAuthenticatedException: AuthenticationException("User not authenticated")