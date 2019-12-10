package com.ledahl.apps.recieppyapi.model

data class Location(
        val id: Long = 0,
        val name: String,
        val address: String?,
        val owner: Int
)