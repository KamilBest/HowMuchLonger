package com.icyapps.howmuchlonger.ui.screen.eventlist.model

import com.icyapps.howmuchlonger.domain.model.Event

data class EventListState(
    val eventEntities: List<Event> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
) 