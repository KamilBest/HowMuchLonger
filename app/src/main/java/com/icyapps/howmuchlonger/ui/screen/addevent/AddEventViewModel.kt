package com.icyapps.howmuchlonger.ui.screen.addevent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.icyapps.howmuchlonger.domain.model.Event
import com.icyapps.howmuchlonger.domain.usecase.AddEventUseCase
import com.icyapps.howmuchlonger.domain.usecase.GetEventByIdUseCase
import com.icyapps.howmuchlonger.domain.usecase.UpdateEventUseCase
import com.icyapps.howmuchlonger.domain.usecase.DeleteEventUseCase
import com.icyapps.howmuchlonger.ui.screen.addevent.intent.AddEventIntent
import com.icyapps.howmuchlonger.ui.screen.addevent.model.AddEventState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException
import javax.inject.Inject
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.Instant
import java.time.LocalTime

@HiltViewModel
class AddEventViewModel @Inject constructor(
    private val addEventUseCase: AddEventUseCase,
    private val getEventByIdUseCase: GetEventByIdUseCase,
    private val updateEventUseCase: UpdateEventUseCase,
    private val deleteEventUseCase: DeleteEventUseCase
) : ViewModel() {

    private var initializedFor: InitializationKey? = null

    private fun getDefaultDate(includeTime: Boolean): Long {
        return if (includeTime) {
            val now = LocalDateTime.now()
            val nextHour = now.plusHours(1).withMinute(0).withSecond(0).withNano(0)
            nextHour.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        } else {
            val tomorrowMidnight = LocalDate.now().plusDays(1).atStartOfDay()
            tomorrowMidnight.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        }
    }

    private val _state = MutableStateFlow(
        AddEventState(
            date = getDefaultDate(true),
            includeTime = true
        )
    )
    val state: StateFlow<AddEventState> = _state.asStateFlow()

    suspend fun initialize(eventId: Long?, initialDate: Long? = null) {
        val initializationKey = InitializationKey(eventId, initialDate)
        if (initializedFor == initializationKey) return

        if (eventId == null) {
            _state.value = AddEventState(
                date = initialDate ?: getDefaultDate(true),
                includeTime = true,
                eventType = com.icyapps.howmuchlonger.domain.model.EventType.Normal
            )
            initializedFor = initializationKey
        } else {
            _state.value = _state.value.copy(isLoading = true)
            try {
                val event = getEventByIdUseCase(eventId)
                if (event != null) {
                    _state.value = _state.value.copy(
                        eventId = event.id,
                        title = event.name,
                        description = event.description,
                        date = event.date,
                        endDate = event.endDate,
                        includeTime = Instant.ofEpochMilli(event.date)
                            .atZone(ZoneId.systemDefault())
                            .toLocalTime() != LocalTime.MIDNIGHT,
                        eventType = event.type,
                        isLoading = false
                    )
                    initializedFor = initializationKey
                } else {
                    _state.value = _state.value.copy(error = "Event not found", isLoading = false)
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _state.value = _state.value.copy(error = e.message, isLoading = false)
            }
        }
    }

    fun processIntent(intent: AddEventIntent) {
        when (intent) {
            is AddEventIntent.UpdateTitle -> updateTitle(intent.title)
            is AddEventIntent.UpdateDescription -> updateDescription(intent.description)
            is AddEventIntent.UpdateDateRange -> updateDateRange(intent.startDate, intent.endDate)
            is AddEventIntent.ToggleIncludeTime -> toggleIncludeTime(intent.include)
            is AddEventIntent.ShowDatePicker -> showDatePicker()
            is AddEventIntent.HideDatePicker -> hideDatePicker()
            is AddEventIntent.ShowTimePicker -> showTimePicker()
            is AddEventIntent.HideTimePicker -> hideTimePicker()
            is AddEventIntent.SaveEvent -> saveEvent()
            is AddEventIntent.NavigateBack -> resetAddEventState()
            is AddEventIntent.DeleteEvent -> deleteEvent()
        }
    }

    private fun resetAddEventState() {
        initializedFor = null
        _state.update {
            AddEventState(
                date = getDefaultDate(true),
                includeTime = true
            )
        }
    }

    private fun updateTitle(title: String) {
        _state.update { it.copy(title = title) }
    }

    private fun updateDescription(description: String) {
        _state.update { it.copy(description = description) }
    }

    private fun updateDateRange(startDate: Long, endDate: Long?) {
        _state.update {
            val normalizedEndDate = endDate?.takeIf { selectedEnd -> selectedEnd > startDate }
            it.copy(
                date = startDate,
                endDate = normalizedEndDate,
                includeTime = if (normalizedEndDate != null && it.endDate == null) {
                    false
                } else {
                    it.includeTime
                },
                showTimePicker = false
            )
        }
    }

    private fun toggleIncludeTime(include: Boolean) {
        _state.update {
            val selectedDate = Instant.ofEpochMilli(it.date)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
            val newDate = if (include) {
                val nextHour = LocalTime.now().plusHours(1).withMinute(0).withSecond(0).withNano(0)
                selectedDate.atTime(nextHour).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            } else {
                selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            }
            it.copy(includeTime = include, date = newDate)
        }
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
        if (state.value.eventType != com.icyapps.howmuchlonger.domain.model.EventType.Normal) return
        if (state.value.title.isBlank()) {
            _state.update { it.copy(error = "Event title cannot be empty") }
            return
        }

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
                        date = currentState.date,
                        type = currentState.eventType,
                        endDate = currentState.endDate
                    )
                    updateEventUseCase(event)
                    // Update state and set saveCompleted flag to trigger navigation
                    _state.update { it.copy(isLoading = false, saveCompleted = true) }
                } else {
                    // Create new event
                    val newEventId = addEventUseCase(
                        name = currentState.title,
                        description = currentState.description,
                        date = currentState.date,
                        endDate = currentState.endDate
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

    private fun deleteEvent() {
        val eventId = state.value.eventId ?: return
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                deleteEventUseCase(eventId)
                _state.update { it.copy(isLoading = false, saveCompleted = true) }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message ?: "Unknown error occurred", isLoading = false) }
            }
        }
    }
}

private data class InitializationKey(
    val eventId: Long?,
    val initialDate: Long?
)
