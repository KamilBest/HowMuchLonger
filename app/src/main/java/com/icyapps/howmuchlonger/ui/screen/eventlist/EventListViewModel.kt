package com.icyapps.howmuchlonger.ui.screen.eventlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.icyapps.howmuchlonger.domain.model.Event
import com.icyapps.howmuchlonger.domain.usecase.DeleteEventUseCase
import com.icyapps.howmuchlonger.domain.usecase.GetEventsUseCase
import com.icyapps.howmuchlonger.ui.screen.eventlist.intent.EventListIntent
import com.icyapps.howmuchlonger.ui.screen.eventlist.model.EventListState
import com.icyapps.howmuchlonger.ui.screen.eventlist.model.EventListTab
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EventListViewModel @Inject constructor(
    private val getEventsUseCase: GetEventsUseCase,
    private val deleteEventUseCase: DeleteEventUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<EventListState>(EventListState.Loading)
    val state: StateFlow<EventListState> = _state.asStateFlow()

    // Cache all events to avoid reloading when switching tabs
    private var allEvents: List<Event> = emptyList()
    private var currentTime: Long = 0

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
                getEventsUseCase().collect { events ->
                    allEvents = events
                    currentTime = System.currentTimeMillis()
                    // Always update to Success state when we receive events
                    val currentState = _state.value
                    val selectedTab = if (currentState is EventListState.Success) {
                        currentState.selectedTab
                    } else {
                        EventListTab.UPCOMING
                    }
                    
                    val filteredEvents = when (selectedTab) {
                        EventListTab.UPCOMING -> allEvents.filter { it.date > currentTime }
                        EventListTab.PAST -> allEvents.filter { it.date <= currentTime }
                    }
                    
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

    private fun switchTab(tab: EventListTab) {
        val currentState = _state.value
        if (currentState is EventListState.Success) {
            val filteredEvents = when (tab) {
                EventListTab.UPCOMING -> allEvents.filter { it.date > currentTime }
                EventListTab.PAST -> allEvents.filter { it.date <= currentTime }
            }
            _state.value = currentState.copy(
                selectedTab = tab,
                events = filteredEvents
            )
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
