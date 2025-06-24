package com.icyapps.howmuchlonger.domain.repository

import com.icyapps.howmuchlonger.domain.model.Event
import kotlinx.coroutines.flow.Flow

interface EventRepository {
    suspend fun getAllEvents(): Flow<List<Event>>
    suspend fun getEventById(id: Long): Event?
    suspend fun insertEvent(event: Event): Long
    suspend fun updateEvent(event: Event)
    suspend fun deleteEvent(event: Event)
    suspend fun getTop3Events(): Flow<List<Event>>
}