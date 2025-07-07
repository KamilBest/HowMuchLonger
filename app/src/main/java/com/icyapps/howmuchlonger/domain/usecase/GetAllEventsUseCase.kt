package com.icyapps.howmuchlonger.domain.usecase

import com.icyapps.howmuchlonger.domain.model.Event
import com.icyapps.howmuchlonger.domain.repository.EventRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllEventsUseCase @Inject constructor(
    private val eventRepository: EventRepository
) {
    suspend operator fun invoke(
        year: Int,
        countryCode: String,
        includeHolidays: Boolean = true
    ): Flow<List<Event>> {
        return eventRepository.getAllEvents(year, countryCode, includeHolidays)
    }
} 