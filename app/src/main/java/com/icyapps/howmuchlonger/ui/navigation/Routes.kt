package com.icyapps.howmuchlonger.ui.navigation

import com.icyapps.howmuchlonger.domain.model.Event

sealed interface Routes {
    data object EventsList : Routes
    data class AddEditEvent(val eventId: Long? = null) : Routes
}
