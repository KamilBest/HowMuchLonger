package com.icyapps.howmuchlonger.domain.model

enum class EventType {
    Normal,
    Holiday
}

data class Event(
    val id: Long = 0,
    val name: String,
    val description: String,
    val date: Long,
    val type: EventType = EventType.Normal
)