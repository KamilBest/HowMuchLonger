package com.icyapps.howmuchlonger.domain.usecase

import com.icyapps.howmuchlonger.domain.model.Event
import com.icyapps.howmuchlonger.domain.model.EventType
import com.icyapps.howmuchlonger.domain.repository.EventRepository
import javax.inject.Inject

class UpdateEventUseCase @Inject constructor(
    private val repository: EventRepository
) {
    suspend operator fun invoke(event: Event) {
        if (event.type == EventType.Normal) {
            repository.updateEvent(event)
        }
    }
}
