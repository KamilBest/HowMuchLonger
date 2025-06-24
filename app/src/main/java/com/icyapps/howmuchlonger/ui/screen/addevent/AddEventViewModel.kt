package com.icyapps.howmuchlonger.ui.screen.addevent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.icyapps.howmuchlonger.domain.usecase.AddEventUseCase
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
    private val addEventUseCase: AddEventUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(AddEventState())
    val state: StateFlow<AddEventState> = _state.asStateFlow()

    fun processIntent(intent: AddEventIntent) {
        when (intent) {
            is AddEventIntent.UpdateTitle -> updateTitle(intent.title)
            is AddEventIntent.UpdateDescription -> updateDescription(intent.description)
            is AddEventIntent.UpdateDate -> updateDate(intent.date)
            is AddEventIntent.SaveEvent -> saveEvent()
        }
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

    private fun saveEvent() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                addEventUseCase(
                    name = state.value.title,
                    description = state.value.description,
                    date = state.value.date
                )
                _state.update { it.copy(isLoading = false, isSuccess = true) }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }
} 