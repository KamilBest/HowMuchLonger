package com.icyapps.howmuchlonger.domain.usecase

import com.icyapps.howmuchlonger.domain.model.Event
import com.icyapps.howmuchlonger.domain.model.EventType
import com.icyapps.howmuchlonger.domain.repository.EventRepository
import javax.inject.Inject

class DeleteEventUseCase @Inject constructor(
    private val repository: EventRepository,
    private val getEventByIdUseCase: GetEventByIdUseCase
) {
    suspend operator fun invoke(eventId: Long) {
        val event = getEventByIdUseCase(eventId)
        event?.takeIf { it.type == EventType.Normal }?.let {
            repository.deleteEvent(it)
        }
    }
}
