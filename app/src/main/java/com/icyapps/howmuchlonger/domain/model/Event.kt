package com.icyapps.howmuchlonger.domain.model

data class Event(
    val id: Long = 0,
    val name: String,
    val description: String,
    val date: Long
)