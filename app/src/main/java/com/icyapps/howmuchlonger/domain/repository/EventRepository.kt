package com.icyapps.howmuchlonger.domain.repository

import com.icyapps.howmuchlonger.data.model.Event
import kotlinx.coroutines.flow.Flow

interface EventRepository {
    fun getAllEvents(): Flow<List<Event>>
    suspend fun getEventById(id: Long): Event?
    suspend fun insertEvent(event: Event): Long
    suspend fun updateEvent(event: Event)
    suspend fun deleteEvent(event: Event)
    fun getTop3Events(): Flow<List<Event>>
} 