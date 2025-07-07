package com.icyapps.howmuchlonger.data.repository

import com.icyapps.howmuchlonger.data.local.EventDao
import com.icyapps.howmuchlonger.data.model.EventEntity
import com.icyapps.howmuchlonger.data.model.PublicHolidayApi
import com.icyapps.howmuchlonger.data.model.toDomainModel
import com.icyapps.howmuchlonger.data.model.toEntity
import com.icyapps.howmuchlonger.domain.model.Event
import com.icyapps.howmuchlonger.domain.model.EventType
import com.icyapps.howmuchlonger.domain.repository.EventRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

class EventRepositoryImpl @Inject constructor(
    private val eventDao: EventDao,
    private val publicHolidayApi: PublicHolidayApi
) : EventRepository {

    override suspend fun getAllEvents(): Flow<List<Event>> {
        return eventDao.getAllEvents().map { entities ->
            entities.map { it.toDomainModel() }
        }.flowOn(Dispatchers.IO)
    }

    override suspend fun getCustomEvents(): Flow<List<Event>> {
        return eventDao.getAllEvents().map { entities ->
            entities.filter { it.type == EventType.Normal }.map { it.toDomainModel() }
        }.flowOn(Dispatchers.IO)
    }

    override suspend fun getHolidayEvents(): Flow<List<Event>> {
        return eventDao.getAllEvents().map { entities ->
            entities.filter { it.type == EventType.Holiday }.map { it.toDomainModel() }
        }.flowOn(Dispatchers.IO)
    }

    override suspend fun refreshHolidaysIfNeeded(year: Int, countryCode: String) {
        val start =
            LocalDate.of(year, 1, 1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val end = LocalDate.of(year, 12, 31).atStartOfDay(ZoneId.systemDefault()).toInstant()
            .toEpochMilli()
        val local = eventDao.getEventsBetween(start, end).filter { it.type == EventType.Holiday }
        if (local.isNotEmpty()) return
        val holidays = publicHolidayApi.getPublicHolidays(year, countryCode)
        val entities = holidays.map { dto ->
            EventEntity(
                name = dto.localName,
                description = dto.name,
                date = LocalDate.parse(dto.date)
                    .atStartOfDay(ZoneId.systemDefault())
                    .toInstant().toEpochMilli(),
                type = EventType.Holiday
            )
        }
        withContext(Dispatchers.IO) {
            entities.forEach { eventDao.insertEvent(it) }
        }
    }

    override suspend fun getEventById(id: Long): Event? {
        return eventDao.getEventById(id)?.toDomainModel()
    }

    override suspend fun insertEvent(event: Event): Long {
        return eventDao.insertEvent(event.toEntity())
    }

    override suspend fun updateEvent(event: Event) {
        eventDao.updateEvent(event.toEntity())
    }

    override suspend fun deleteEvent(event: Event) {
        eventDao.deleteEvent(event.toEntity())
    }

    override suspend fun getTop3Events(): Flow<List<Event>> {
        return eventDao.getTop3Events().map { entities ->
            entities.map { it.toDomainModel() }
        }.flowOn(Dispatchers.IO)
    }
}