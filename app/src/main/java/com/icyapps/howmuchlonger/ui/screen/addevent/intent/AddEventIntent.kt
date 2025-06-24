package com.icyapps.howmuchlonger.ui.screen.addevent.intent

sealed class AddEventIntent {
    data class UpdateTitle(val title: String) : AddEventIntent()
    data class UpdateDescription(val description: String) : AddEventIntent()
    data class UpdateDate(val date: Long) : AddEventIntent()
    object SaveEvent : AddEventIntent()
}