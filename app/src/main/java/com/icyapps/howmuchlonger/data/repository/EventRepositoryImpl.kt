package com.icyapps.howmuchlonger.data.repository

import com.icyapps.howmuchlonger.data.local.EventDataStore
import com.icyapps.howmuchlonger.data.model.EventEntity
import com.icyapps.howmuchlonger.data.model.toDomainModel
import com.icyapps.howmuchlonger.data.model.toEntity
import com.icyapps.howmuchlonger.data.source.PublicHolidayDataSource
import com.icyapps.howmuchlonger.data.store.PublicHolidayDataStore
import com.icyapps.howmuchlonger.domain.model.Event
import com.icyapps.howmuchlonger.domain.model.EventType
import com.icyapps.howmuchlonger.domain.repository.EventRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

class EventRepositoryImpl @Inject constructor(
    private val eventDataStore: EventDataStore,
    private val publicHolidayDataSource: PublicHolidayDataSource,
    private val publicHolidayDataStore: PublicHolidayDataStore
) : EventRepository {

    override suspend fun getAllEvents(year: Int, countryCode: String, includeHolidays: Boolean): Flow<List<Event>> {
        val customEvents = getCustomEvents()
        return if (includeHolidays) {
            val holidays = getHolidays(year, countryCode)
            combine(customEvents, holidays) { custom, holiday ->
                (custom + holiday).sortedBy { it.date }
            }.flowOn(Dispatchers.IO)
        } else {
            customEvents
        }
    }

    private suspend fun getHolidays(year: Int, countryCode: String): Flow<List<Event>> {
        return flow {
            val start = LocalDate.of(year, 1, 1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val end = LocalDate.of(year, 12, 31).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val local = publicHolidayDataStore.getHolidaysBetween(start, end)
            if (local.isNotEmpty()) {
                emit(local.map { it.toDomainModel() })
            } else {
                // Otherwise fetch from API and cache
                val holidays = publicHolidayDataSource.getPublicHolidays(year, countryCode)
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
                    publicHolidayDataStore.insertHolidays(entities)
                }
                emit(publicHolidayDataStore.getHolidaysBetween(start, end).map { it.toDomainModel() })
            }
        }.flowOn(Dispatchers.IO)
    }

    override suspend fun getCustomEvents(): Flow<List<Event>> {
        return eventDataStore.getAllEvents().map { entities ->
            entities.filter { it.type == EventType.Normal }.map { it.toDomainModel() }
        }.flowOn(Dispatchers.IO)
    }

    override suspend fun getHolidayEvents(): Flow<List<Event>> {
        return eventDataStore.getAllEvents().map { entities ->
            entities.filter { it.type == EventType.Holiday }.map { it.toDomainModel() }
        }.flowOn(Dispatchers.IO)
    }

    override suspend fun refreshHolidaysIfNeeded(year: Int, countryCode: String) {
        // This method is now obsolete in the new architecture
    }

    override suspend fun getEventById(id: Long): Event? {
        return eventDataStore.getEventById(id)?.toDomainModel()
    }

    override suspend fun insertEvent(event: Event): Long {
        return eventDataStore.insertEvent(event.toEntity())
    }

    override suspend fun updateEvent(event: Event) {
        eventDataStore.updateEvent(event.toEntity())
    }

    override suspend fun deleteEvent(event: Event) {
        eventDataStore.deleteEvent(event.toEntity())
    }

    override suspend fun getTop3Events(): Flow<List<Event>> {
        return eventDataStore.getTop3Events().map { entities ->
            entities.map { it.toDomainModel() }
        }.flowOn(Dispatchers.IO)
    }
}