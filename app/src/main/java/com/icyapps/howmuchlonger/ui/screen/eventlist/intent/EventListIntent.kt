package com.icyapps.howmuchlonger.ui.screen.eventlist.intent

import com.icyapps.howmuchlonger.ui.screen.eventlist.model.EventListTab
import java.time.LocalDate

sealed class EventListIntent {
    object LoadEvents : EventListIntent()
    data class DeleteEvent(val eventId: Long) : EventListIntent()
    data class SwitchTab(val tab: EventListTab) : EventListIntent()
    data object ToggleHolidays : EventListIntent()
    data object ToggleCalendar : EventListIntent()
    data class ChangeCalendarMonth(val months: Long) : EventListIntent()
    data class SelectCalendarDate(val date: LocalDate) : EventListIntent()
}
