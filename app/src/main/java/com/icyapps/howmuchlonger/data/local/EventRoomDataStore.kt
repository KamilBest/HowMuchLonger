package com.icyapps.howmuchlonger.data.local

import com.icyapps.howmuchlonger.data.model.EventEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class EventRoomDataStore @Inject constructor(
    private val eventDao: EventDao
) : EventDataStore {
    
    override fun getAllEvents(): Flow<List<EventEntity>> {
        return eventDao.getAllEvents()
    }
    
    override fun getTop3Events(): Flow<List<EventEntity>> {
        return eventDao.getTop3Events()
    }
    
    override suspend fun getEventById(id: Long): EventEntity? {
        return eventDao.getEventById(id)
    }
    
    override suspend fun insertEvent(event: EventEntity): Long {
        return eventDao.insertEvent(event)
    }
    
    override suspend fun updateEvent(event: EventEntity) {
        eventDao.updateEvent(event)
    }
    
    override suspend fun deleteEvent(event: EventEntity) {
        eventDao.deleteEvent(event)
    }
} 