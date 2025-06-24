package com.icyapps.howmuchlonger.domain.usecase

import com.icyapps.howmuchlonger.domain.model.Event
import com.icyapps.howmuchlonger.domain.repository.EventRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTop3EventsUseCase @Inject constructor(
    private val repository: EventRepository
) {
    suspend operator fun invoke(): Flow<List<Event>> = repository.getTop3Events()
} 