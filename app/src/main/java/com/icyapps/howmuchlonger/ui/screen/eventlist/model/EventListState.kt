package com.icyapps.howmuchlonger.ui.screen.eventlist.model

import com.icyapps.howmuchlonger.domain.model.Event

enum class EventListTab {
    UPCOMING, PAST
}

sealed class EventListState {
    object Loading : EventListState()

    data class Success(
        val events: List<Event> = emptyList(),
        val selectedTab: EventListTab = EventListTab.UPCOMING
    ) : EventListState()

    data class Error(
        val message: String
    ) : EventListState()
}
