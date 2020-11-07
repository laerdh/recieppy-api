package com.ledahl.apps.recieppyapi.model

import java.time.LocalDateTime

data class LocationInvite(val id: Long,
                          val sent: LocalDateTime,
                          val locationId: Long,
                          val email: String,
                          val inviteCode: String,
                          val acceptedByUser: Long? = null)