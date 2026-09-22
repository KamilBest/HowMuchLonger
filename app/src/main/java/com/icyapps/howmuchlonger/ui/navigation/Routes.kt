package com.icyapps.howmuchlonger.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface Routes : NavKey {
    @Serializable
    data object EventsList : Routes

    @Serializable
    data class AddEditEvent(
        val eventId: Long? = null,
        val initialDate: Long? = null
    ) : Routes
}
