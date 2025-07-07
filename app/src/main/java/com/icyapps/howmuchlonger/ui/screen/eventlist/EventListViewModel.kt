package com.icyapps.howmuchlonger.ui.screen.eventlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.icyapps.howmuchlonger.domain.model.Event
import com.icyapps.howmuchlonger.domain.usecase.DeleteEventUseCase
import com.icyapps.howmuchlonger.domain.repository.EventRepository
import com.icyapps.howmuchlonger.ui.screen.eventlist.intent.EventListIntent
import com.icyapps.howmuchlonger.ui.screen.eventlist.model.EventListState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.util.Locale
import java.util.Calendar
import com.icyapps.howmuchlonger.domain.usecase.GetAllEventsUseCase

@HiltViewModel
class EventListViewModel @Inject constructor(
    private val getAllEventsUseCase: GetAllEventsUseCase,
    private val deleteEventUseCase: DeleteEventUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<EventListState>(EventListState.Loading)
    val state: StateFlow<EventListState> = _state.asStateFlow()

    private var allEvents: List<Event> = emptyList()
    private var selectedTab: com.icyapps.howmuchlonger.ui.screen.eventlist.model.EventListTab = com.icyapps.howmuchlonger.ui.screen.eventlist.model.EventListTab.UPCOMING
    private var includeHolidays: Boolean = true

    fun processIntent(intent: EventListIntent) {
        when (intent) {
            is EventListIntent.LoadEvents -> loadEvents()
            is EventListIntent.DeleteEvent -> deleteEvent(intent.eventId)
            is EventListIntent.SwitchTab -> switchTab(intent.tab)
        }
    }

    private fun loadEvents() {
        viewModelScope.launch {
            _state.value = EventListState.Loading
            try {
                val year = Calendar.getInstance().get(Calendar.YEAR)
                val countryCode = Locale.getDefault().country
                getAllEventsUseCase(year, countryCode, includeHolidays).collect { events ->
                    allEvents = events
                    val filteredEvents = filterEvents(selectedTab, allEvents)
                    _state.value = EventListState.Success(
                        events = filteredEvents,
                        selectedTab = selectedTab
                    )
                }
            } catch (e: Exception) {
                _state.value = EventListState.Error(e.message ?: "Unknown error")
            }
        }
    }

    private fun switchTab(tab: com.icyapps.howmuchlonger.ui.screen.eventlist.model.EventListTab) {
        selectedTab = tab
        val filteredEvents = filterEvents(selectedTab, allEvents)
        val currentState = _state.value
        if (currentState is EventListState.Success) {
            _state.value = currentState.copy(
                selectedTab = selectedTab,
                events = filteredEvents
            )
        }
    }

    private fun filterEvents(tab: com.icyapps.howmuchlonger.ui.screen.eventlist.model.EventListTab, events: List<Event>): List<Event> {
        val now = System.currentTimeMillis()
        return when (tab) {
            com.icyapps.howmuchlonger.ui.screen.eventlist.model.EventListTab.UPCOMING -> events.filter { it.date > now }.sortedBy { it.date }
            com.icyapps.howmuchlonger.ui.screen.eventlist.model.EventListTab.PAST -> events.filter { it.date <= now }.sortedByDescending { it.date }
        }
    }

    private fun deleteEvent(eventId: Long) {
        viewModelScope.launch {
            _state.value = EventListState.Loading
            try {
                deleteEventUseCase(eventId)
                loadEvents() // Reload events after deletion
            } catch (e: Exception) {
                _state.value = EventListState.Error(e.message ?: "Unknown error")
            }
        }
    }
}
