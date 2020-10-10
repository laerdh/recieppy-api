package com.ledahl.apps.recieppyapi.model.mappers

interface Mapper<T, V> {
    fun map(item: T): V
}