package com.icyapps.howmuchlonger.ui.screen.addevent.model

data class AddEventState(
    val eventId: Long? = null,
    val title: String = "",
    val description: String = "",
    val date: Long = System.currentTimeMillis(),
    val eventType: com.icyapps.howmuchlonger.domain.model.EventType = com.icyapps.howmuchlonger.domain.model.EventType.Normal,
    val includeTime: Boolean = true,
    val showDatePicker: Boolean = false,
    val showTimePicker: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val saveCompleted: Boolean = false
)
