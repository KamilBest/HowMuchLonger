package com.icyapps.howmuchlonger.ui.screen.eventlist.intent

sealed class EventListIntent {
    object LoadEvents : EventListIntent()
    data class DeleteEvent(val eventId: Long) : EventListIntent()
    object AddNewEvent : EventListIntent()
} 