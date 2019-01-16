package com.ledahl.apps.recieppyapi.model

data class User(val id: Long,
                val firstName: String,
                val lastName: String,
                val email: String? = null,
                val firebaseId: String? = null,
                val phoneNumber: String,
                val token: String? = null)