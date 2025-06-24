package com.icyapps.howmuchlonger.domain.usecase

import com.icyapps.howmuchlonger.domain.model.Event
import com.icyapps.howmuchlonger.domain.repository.EventRepository
import javax.inject.Inject

class AddEventUseCase @Inject constructor(
    private val repository: EventRepository
) {
    suspend operator fun invoke(name: String, description: String, date: Long): Long {
        return repository.insertEvent(
            Event(
                name = name,
                description = description,
                date = date
            )
        )
    }
} 