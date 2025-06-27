package com.icyapps.howmuchlonger.ui.screen.eventlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.icyapps.howmuchlonger.domain.usecase.DeleteEventUseCase
import com.icyapps.howmuchlonger.domain.usecase.GetEventsUseCase
import com.icyapps.howmuchlonger.ui.screen.eventlist.intent.EventListIntent
import com.icyapps.howmuchlonger.ui.screen.eventlist.model.EventListState
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

    private val _state = MutableStateFlow<EventListState>(EventListState.Success())
    val state: StateFlow<EventListState> = _state.asStateFlow()

    fun processIntent(intent: EventListIntent) {
        when (intent) {
            is EventListIntent.LoadEvents -> loadEvents()
            is EventListIntent.DeleteEvent -> deleteEvent(intent.eventId)
        }
    }

    private fun loadEvents() {
        viewModelScope.launch {
            _state.value = EventListState.Loading
            try {
                getEventsUseCase().collect { events ->
                    val currentTime = System.currentTimeMillis()
                    val (upcoming, past) = events.partition { event -> 
                        event.date > currentTime 
                    }
                    _state.value = EventListState.Success(
                        upcomingEvents = upcoming,
                        pastEvents = past
                    )
                }
            } catch (e: Exception) {
                _state.value = EventListState.Error(e.message ?: "Unknown error")
            }
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
