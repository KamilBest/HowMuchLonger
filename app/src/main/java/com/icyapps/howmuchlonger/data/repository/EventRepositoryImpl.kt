package com.icyapps.howmuchlonger.data.repository

import com.icyapps.howmuchlonger.data.local.EventDao
import com.icyapps.howmuchlonger.data.model.Event
import com.icyapps.howmuchlonger.domain.repository.EventRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class EventRepositoryImpl @Inject constructor(
    private val eventDao: EventDao
) : EventRepository {
    override fun getAllEvents(): Flow<List<Event>> = eventDao.getAllEvents()

    override suspend fun getEventById(id: Long): Event? = eventDao.getEventById(id)

    override suspend fun insertEvent(event: Event): Long = eventDao.insertEvent(event)

    override suspend fun updateEvent(event: Event) = eventDao.updateEvent(event)

    override suspend fun deleteEvent(event: Event) = eventDao.deleteEvent(event)

    override fun getTop3Events(): Flow<List<Event>> = eventDao.getTop3Events()
} 