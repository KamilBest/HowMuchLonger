package com.icyapps.howmuchlonger.ui.screen.eventlist.model

import com.icyapps.howmuchlonger.domain.model.Event

sealed class EventListState {
    object Loading : EventListState()

    data class Success(
        val events: List<Event> = emptyList(),
        val selectedTab: EventListTab = EventListTab.UPCOMING,
        val includeHolidays: Boolean = true,
        val allEvents: List<Event> = emptyList()
    ) : EventListState()

    data class Error(
        val message: String
    ) : EventListState()
}
