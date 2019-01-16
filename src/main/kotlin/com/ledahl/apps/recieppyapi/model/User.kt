package com.ledahl.apps.recieppyapi.model

data class User(val id: Long,
                val name: String,
                val firebaseId: String? = null,
                val phoneNumber: String,
                val token: String? = null)