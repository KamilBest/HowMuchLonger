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
import com.icyapps.howmuchlonger.domain.usecase.GetEventsUseCase
import com.icyapps.howmuchlonger.ui.screen.eventlist.model.EventListTab

@HiltViewModel
class EventListViewModel @Inject constructor(
    private val getEventsUseCase: GetEventsUseCase,
    private val deleteEventUseCase: DeleteEventUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<EventListState>(EventListState.Loading)
    val state: StateFlow<EventListState> = _state.asStateFlow()

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
                val currentState = _state.value
                val selectedTab = if (currentState is EventListState.Success) currentState.selectedTab else EventListTab.UPCOMING
                val includeHolidays = if (currentState is EventListState.Success) currentState.includeHolidays else true
                getEventsUseCase(year, countryCode, includeHolidays).collect { events ->
                    val filteredEvents = filterEvents(selectedTab, events)
                    _state.value = EventListState.Success(
                        events = filteredEvents,
                        selectedTab = selectedTab,
                        includeHolidays = includeHolidays,
                        allEvents = events
                    )
                }
            } catch (e: Exception) {
                _state.value = EventListState.Error(e.message ?: "Unknown error")
            }
        }
    }

    private fun switchTab(tab: EventListTab) {
        val currentState = _state.value
        if (currentState is EventListState.Success) {
            val filteredEvents = filterEvents(tab, currentState.allEvents)
            _state.value = currentState.copy(
                selectedTab = tab,
                events = filteredEvents
            )
        }
    }

    private fun filterEvents(tab: EventListTab, events: List<Event>): List<Event> {
        val now = System.currentTimeMillis()
        return when (tab) {
            EventListTab.UPCOMING -> events.filter { it.date > now }.sortedBy { it.date }
            EventListTab.PAST -> events.filter { it.date <= now }.sortedByDescending { it.date }
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
