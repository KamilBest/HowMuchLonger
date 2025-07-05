package com.icyapps.howmuchlonger.ui.screen.addevent

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.icyapps.howmuchlonger.domain.model.Event
import com.icyapps.howmuchlonger.domain.usecase.AddEventUseCase
import com.icyapps.howmuchlonger.domain.usecase.GetEventByIdUseCase
import com.icyapps.howmuchlonger.domain.usecase.UpdateEventUseCase
import com.icyapps.howmuchlonger.ui.screen.addevent.intent.AddEventIntent
import com.icyapps.howmuchlonger.ui.screen.addevent.model.AddEventState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddEventViewModel @Inject constructor(
    private val addEventUseCase: AddEventUseCase,
    private val getEventByIdUseCase: GetEventByIdUseCase,
    private val updateEventUseCase: UpdateEventUseCase,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(AddEventState())
    val state: StateFlow<AddEventState> = _state.asStateFlow()


    init {
        viewModelScope.launch {
            savedStateHandle.getStateFlow<Long?>("eventId", null).collect { eventId ->
                if (eventId != null) {
                    loadEvent(eventId)
                }
            }
        }
    }

    private fun loadEvent(eventId: Long) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val event = getEventByIdUseCase(eventId)
                if (event != null) {
                    _state.update {
                        it.copy(
                            eventId = event.id,
                            title = event.name,
                            description = event.description,
                            date = event.date,
                            isLoading = false
                        )
                    }
                } else {
                    _state.update { it.copy(error = "Event not found", isLoading = false) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    fun processIntent(intent: AddEventIntent) {
        when (intent) {
            is AddEventIntent.UpdateTitle -> updateTitle(intent.title)
            is AddEventIntent.UpdateDescription -> updateDescription(intent.description)
            is AddEventIntent.UpdateDate -> updateDate(intent.date)
            is AddEventIntent.ToggleIncludeTime -> toggleIncludeTime(intent.include)
            is AddEventIntent.ShowDatePicker -> showDatePicker()
            is AddEventIntent.HideDatePicker -> hideDatePicker()
            is AddEventIntent.ShowTimePicker -> showTimePicker()
            is AddEventIntent.HideTimePicker -> hideTimePicker()
            is AddEventIntent.SaveEvent -> saveEvent()
            is AddEventIntent.NavigateBack -> resetAddEventState()
        }
    }

    private fun resetAddEventState() {
        _state.update { AddEventState() }
    }

    private fun updateTitle(title: String) {
        _state.update { it.copy(title = title) }
    }

    private fun updateDescription(description: String) {
        _state.update { it.copy(description = description) }
    }

    private fun updateDate(date: Long) {
        _state.update { it.copy(date = date) }
    }

    private fun toggleIncludeTime(include: Boolean) {
        _state.update { it.copy(includeTime = include) }
    }

    private fun showDatePicker() {
        _state.update { it.copy(showDatePicker = true) }
    }

    private fun hideDatePicker() {
        _state.update { it.copy(showDatePicker = false) }
    }

    private fun showTimePicker() {
        _state.update { it.copy(showTimePicker = true) }
    }

    private fun hideTimePicker() {
        _state.update { it.copy(showTimePicker = false) }
    }

    private fun saveEvent() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val currentState = state.value
                if (currentState.eventId != null) {
                    // Update existing event
                    val event = Event(
                        id = currentState.eventId,
                        name = currentState.title,
                        description = currentState.description,
                        date = currentState.date
                    )
                    updateEventUseCase(event)
                    // Update state and set saveCompleted flag to trigger navigation
                    _state.update { it.copy(isLoading = false, saveCompleted = true) }
                } else {
                    // Create new event
                    val newEventId = addEventUseCase(
                        name = currentState.title,
                        description = currentState.description,
                        date = currentState.date
                    )
                    // Update state with the new event ID and set saveCompleted flag to trigger navigation
                    _state.update {
                        it.copy(
                            eventId = newEventId,
                            isLoading = false,
                            saveCompleted = true
                        )
                    }
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        error = e.message ?: "Unknown error occurred",
                        isLoading = false
                    )
                }
            }
        }
    }
} 
