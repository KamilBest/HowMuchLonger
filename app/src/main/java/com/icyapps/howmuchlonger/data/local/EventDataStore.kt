package com.icyapps.howmuchlonger.data.local

import com.icyapps.howmuchlonger.data.model.EventEntity
import kotlinx.coroutines.flow.Flow

interface EventDataStore {
    fun getAllEvents(): Flow<List<EventEntity>>
    fun getTop3Events(): Flow<List<EventEntity>>
    suspend fun getEventById(id: Long): EventEntity?
    suspend fun insertEvent(event: EventEntity): Long
    suspend fun updateEvent(event: EventEntity)
    suspend fun deleteEvent(event: EventEntity)
} 