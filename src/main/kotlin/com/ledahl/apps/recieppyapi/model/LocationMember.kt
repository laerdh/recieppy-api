package com.ledahl.apps.recieppyapi.model

data class LocationMember(val firstName: String,
                          val lastName: String,
                          val email: String? = null,
                          val isOwner: Boolean = false)