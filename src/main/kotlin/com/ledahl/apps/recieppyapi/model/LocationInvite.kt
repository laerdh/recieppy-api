package com.ledahl.apps.recieppyapi.model

import java.time.LocalDateTime

data class LocationInvite(val id: Long,
                          val timeSent: LocalDateTime? = null,
                          val invitedBy: Long,
                          val locationId: Long,
                          val email: String,
                          val inviteCode: String,
                          val acceptedByUser: Long? = null)