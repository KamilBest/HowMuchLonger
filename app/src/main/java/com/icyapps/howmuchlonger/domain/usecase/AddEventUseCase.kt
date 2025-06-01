package com.icyapps.howmuchlonger.domain.usecase

import com.icyapps.howmuchlonger.data.model.Event
import com.icyapps.howmuchlonger.domain.repository.EventRepository
import javax.inject.Inject

class AddEventUseCase @Inject constructor(
    private val repository: EventRepository
) {
    suspend operator fun invoke(name: String, description: String, date: Long): Long {
        val event = Event(
            name = name,
            description = description,
            date = date
        )
        return repository.insertEvent(event)
    }
} 