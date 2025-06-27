package com.icyapps.howmuchlonger.ui.screen.addevent.model

data class AddEventState(
    val eventId: Long? = null,
    val title: String = "",
    val description: String = "",
    val date: Long = System.currentTimeMillis(),
    val includeTime: Boolean = true,
    val showDatePicker: Boolean = false,
    val showTimePicker: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)
