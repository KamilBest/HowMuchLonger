package com.icyapps.howmuchlonger.ui.screen.eventlist.model

import com.icyapps.howmuchlonger.domain.model.Event
import com.icyapps.howmuchlonger.domain.model.HolidayCountry
import java.time.YearMonth
import java.time.LocalDate

sealed class EventListState {
    object Loading : EventListState()

    data class Success(
        val events: List<Event> = emptyList(),
        val selectedTab: EventListTab = EventListTab.UPCOMING,
        val includeHolidays: Boolean = true,
        val allEvents: List<Event> = emptyList(),
        val calendarMonth: YearMonth = YearMonth.now(),
        val selectedCalendarDate: LocalDate = LocalDate.now(),
        val showCalendar: Boolean = false,
        val holidayCountryCodes: List<String> = listOf(HolidayCountry.DEFAULT_CODE),
        val holidayCountries: List<HolidayCountry> = emptyList(),
        val holidayCountriesLoadFailed: Boolean = false
    ) : EventListState()

    data class Error(
        val message: String
    ) : EventListState()
}
