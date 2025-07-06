package com.icyapps.howmuchlonger.ui.screen.eventlist.intent

import com.icyapps.howmuchlonger.ui.screen.eventlist.model.EventListTab

sealed class EventListIntent {
    object LoadEvents : EventListIntent()
    data class DeleteEvent(val eventId: Long) : EventListIntent()
    data class SwitchTab(val tab: EventListTab) : EventListIntent()
}
