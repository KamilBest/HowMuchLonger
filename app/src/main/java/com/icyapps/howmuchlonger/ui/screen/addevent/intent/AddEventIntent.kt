package com.icyapps.howmuchlonger.ui.screen.addevent.intent

sealed class AddEventIntent {
    data class UpdateTitle(val title: String) : AddEventIntent()
    data class UpdateDescription(val description: String) : AddEventIntent()
    data class UpdateDate(val date: Long) : AddEventIntent()
    data class ToggleIncludeTime(val include: Boolean) : AddEventIntent()
    object ShowDatePicker : AddEventIntent()
    object HideDatePicker : AddEventIntent()
    object ShowTimePicker : AddEventIntent()
    object HideTimePicker : AddEventIntent()
    object SaveEvent : AddEventIntent()
}
