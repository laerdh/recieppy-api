package com.ledahl.apps.recieppyapi.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "spring.security.oauth2.resourceserver.jwt")
data class JwtProperties(val issuers: List<JwtIssuerProperties>)