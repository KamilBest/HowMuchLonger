package com.icyapps.howmuchlonger.ui.screen.eventlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.icyapps.howmuchlonger.domain.model.Event
import com.icyapps.howmuchlonger.domain.usecase.DeleteEventUseCase
import com.icyapps.howmuchlonger.domain.model.EventType
import com.icyapps.howmuchlonger.ui.screen.eventlist.intent.EventListIntent
import com.icyapps.howmuchlonger.ui.screen.eventlist.model.EventListState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.CancellationException
import javax.inject.Inject
import java.time.YearMonth
import java.time.LocalDate
import com.icyapps.howmuchlonger.domain.usecase.GetEventsUseCase
import com.icyapps.howmuchlonger.domain.usecase.GetAvailableHolidayCountriesUseCase
import com.icyapps.howmuchlonger.domain.usecase.GetHolidayCountriesUseCase
import com.icyapps.howmuchlonger.domain.usecase.SetHolidayCountriesUseCase
import com.icyapps.howmuchlonger.ui.screen.eventlist.model.EventListTab

@HiltViewModel
class EventListViewModel @Inject constructor(
    private val getEventsUseCase: GetEventsUseCase,
    private val deleteEventUseCase: DeleteEventUseCase,
    private val getHolidayCountriesUseCase: GetHolidayCountriesUseCase,
    private val getAvailableHolidayCountriesUseCase: GetAvailableHolidayCountriesUseCase,
    private val setHolidayCountriesUseCase: SetHolidayCountriesUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<EventListState>(EventListState.Loading)
    val state: StateFlow<EventListState> = _state.asStateFlow()
    private var loadJob: Job? = null

    fun processIntent(intent: EventListIntent) {
        when (intent) {
            is EventListIntent.LoadEvents -> loadEvents()
            is EventListIntent.DeleteEvent -> deleteEvent(intent.eventId)
            is EventListIntent.SwitchTab -> switchTab(intent.tab)
            EventListIntent.ToggleHolidays -> toggleHolidays()
            EventListIntent.ToggleCalendar -> toggleCalendar()
            is EventListIntent.ChangeCalendarMonth -> changeCalendarMonth(intent.months)
            is EventListIntent.SelectCalendarDate -> selectCalendarDate(intent.date)
            EventListIntent.LoadHolidayCountries -> loadHolidayCountries()
            is EventListIntent.SelectHolidayCountries -> selectHolidayCountries(intent.countryCodes)
        }
    }

    private fun loadEvents() {
        val previousState = _state.value as? EventListState.Success
        _state.value = EventListState.Loading
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            try {
                val countryCodes = getHolidayCountriesUseCase()
                val selectedTab = previousState?.selectedTab ?: EventListTab.UPCOMING
                val includeHolidays = previousState?.includeHolidays ?: true
                val calendarMonth = previousState?.calendarMonth ?: YearMonth.now()
                val currentYear = LocalDate.now().year
                val eventFlows = countryCodes.flatMap { countryCode ->
                    listOf(currentYear, currentYear + 1).map { year -> getEventsUseCase(year, countryCode, true) }
                }
                combine(eventFlows) { eventLists -> mergeEvents(eventLists.flatMap { it }) }.collect { events ->
                    val latestState = _state.value as? EventListState.Success
                    val currentTab = latestState?.selectedTab ?: selectedTab
                    val currentIncludeHolidays = latestState?.includeHolidays ?: includeHolidays
                    val currentCalendarMonth = latestState?.calendarMonth ?: calendarMonth
                    val selectedCalendarDate = latestState?.selectedCalendarDate
                        ?: previousState?.selectedCalendarDate
                        ?: java.time.LocalDate.now()
                    val showCalendar = latestState?.showCalendar ?: previousState?.showCalendar ?: false
                    val filteredEvents = filterEvents(currentTab, events, currentIncludeHolidays)
                    _state.value = EventListState.Success(
                        events = filteredEvents,
                        selectedTab = currentTab,
                        includeHolidays = currentIncludeHolidays,
                        allEvents = events,
                        calendarMonth = currentCalendarMonth,
                        selectedCalendarDate = selectedCalendarDate,
                        showCalendar = showCalendar,
                        holidayCountryCodes = countryCodes,
                        holidayCountries = latestState?.holidayCountries ?: previousState?.holidayCountries.orEmpty(),
                        holidayCountriesLoadFailed = latestState?.holidayCountriesLoadFailed
                            ?: previousState?.holidayCountriesLoadFailed
                            ?: false
                    )
                }
            } catch (error: CancellationException) {
                throw error
            } catch (e: Exception) {
                _state.value = EventListState.Error(e.message ?: "Unknown error")
            }
        }
    }

    private fun switchTab(tab: EventListTab) {
        val currentState = _state.value
        if (currentState is EventListState.Success) {
            val filteredEvents = filterEvents(tab, currentState.allEvents, currentState.includeHolidays)
            _state.value = currentState.copy(
                selectedTab = tab,
                events = filteredEvents,
                showCalendar = false
            )
        }
    }

    private fun toggleHolidays() {
        val currentState = _state.value as? EventListState.Success ?: return
        val includeHolidays = !currentState.includeHolidays
        _state.value = currentState.copy(
            includeHolidays = includeHolidays,
            events = filterEvents(currentState.selectedTab, currentState.allEvents, includeHolidays)
        )
    }

    private fun loadHolidayCountries() {
        val currentState = _state.value as? EventListState.Success ?: return
        if (currentState.holidayCountries.isNotEmpty()) return
        _state.value = currentState.copy(holidayCountriesLoadFailed = false)
        viewModelScope.launch {
            val countries = getAvailableHolidayCountriesUseCase()
            val latestState = _state.value as? EventListState.Success ?: return@launch
            _state.value = latestState.copy(
                holidayCountries = countries,
                holidayCountriesLoadFailed = countries.isEmpty()
            )
        }
    }

    private fun selectHolidayCountries(countryCodes: Set<String>) {
        val currentState = _state.value as? EventListState.Success ?: return
        if (countryCodes.isEmpty()) return
        // Persist even an unchanged selection, so an explicit choice no longer follows the device language.
        setHolidayCountriesUseCase(countryCodes)
        if (currentState.holidayCountryCodes.toSet() != countryCodes) loadEvents()
    }

    private fun toggleCalendar() {
        val currentState = _state.value as? EventListState.Success ?: return
        _state.value = currentState.copy(showCalendar = !currentState.showCalendar)
    }

    private fun selectCalendarDate(date: LocalDate) {
        val currentState = _state.value as? EventListState.Success ?: return
        if (date.isAfter(LocalDate.now().plusYears(1))) return
        _state.value = currentState.copy(selectedCalendarDate = date)
    }

    private fun changeCalendarMonth(months: Long) {
        val currentState = _state.value as? EventListState.Success ?: return
        val newMonth = currentState.calendarMonth.plusMonths(months)
        if (newMonth > YearMonth.from(LocalDate.now().plusYears(1))) return
        _state.value = currentState.copy(
            calendarMonth = newMonth,
            selectedCalendarDate = newMonth.atDay(1)
        )
    }

    private fun filterEvents(
        tab: EventListTab,
        events: List<Event>,
        includeHolidays: Boolean
    ): List<Event> {
        val now = System.currentTimeMillis()
        val upcomingEndExclusive = LocalDate.now()
            .plusYears(1)
            .plusDays(1)
            .atStartOfDay(java.time.ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        val visibleEvents = events.filter { includeHolidays || it.type != EventType.Holiday }
        return when (tab) {
            EventListTab.UPCOMING -> visibleEvents
                .filter { eventEndBoundary(it) > now && it.date < upcomingEndExclusive }
                .sortedBy { it.date }
            EventListTab.PAST -> visibleEvents
                .filter { eventEndBoundary(it) <= now }
                .sortedByDescending { eventEndBoundary(it) }
        }
    }

    private fun mergeEvents(events: List<Event>): List<Event> =
        events
            .distinctBy { event ->
                when (event.type) {
                    EventType.Normal -> EventIdentity(type = event.type, id = event.id)
                    EventType.Holiday -> EventIdentity(
                        type = event.type,
                        date = event.date,
                        name = event.name,
                        countryCode = event.countryCode
                    )
                }
            }
            .sortedBy { it.date }

    private fun eventEndBoundary(event: Event): Long =
        when {
            event.type == EventType.Holiday ->
                java.time.Instant.ofEpochMilli(event.date)
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDate()
                    .plusDays(1)
                    .atStartOfDay(java.time.ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli()
            event.endDate != null -> event.endDate.let { endDate ->
                java.time.Instant.ofEpochMilli(endDate)
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDate()
                    .plusDays(1)
                    .atStartOfDay(java.time.ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli()
            }
            else -> event.date
        }

    private fun deleteEvent(eventId: Long) {
        viewModelScope.launch {
            val currentState = _state.value as? EventListState.Success
            val event = currentState?.allEvents?.firstOrNull { it.id == eventId }
            if (event?.type != EventType.Normal) return@launch
            try {
                deleteEventUseCase(eventId)
            } catch (e: Exception) {
                _state.value = EventListState.Error(e.message ?: "Unknown error")
            }
        }
    }
}

private data class EventIdentity(
    val type: EventType,
    val id: Long = 0,
    val date: Long = 0,
    val name: String = "",
    val countryCode: String? = null
)
