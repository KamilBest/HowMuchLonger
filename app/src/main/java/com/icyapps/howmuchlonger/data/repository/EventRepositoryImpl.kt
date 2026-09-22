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
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.LocalDate
import java.time.ZoneId
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

class EventRepositoryImpl @Inject constructor(
    private val eventDataStore: EventDataStore,
    private val publicHolidayDataSource: PublicHolidayDataSource,
    private val publicHolidayDataStore: PublicHolidayDataStore
) : EventRepository {

    private val holidayMemoryCache = ConcurrentHashMap<HolidayCacheKey, List<Event>>()
    private val holidayLoadLocks = ConcurrentHashMap<HolidayCacheKey, Mutex>()

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
            val normalizedCountryCode = countryCode.uppercase().takeIf { it.length == 2 } ?: "PL"
            val cacheKey = HolidayCacheKey(year, normalizedCountryCode)
            holidayMemoryCache[cacheKey]?.let { cached ->
                emit(cached)
                return@flow
            }

            val loadLock = holidayLoadLocks.computeIfAbsent(cacheKey) { Mutex() }
            val holidays = loadLock.withLock {
                holidayMemoryCache[cacheKey] ?: loadHolidays(cacheKey)
            }
            emit(holidays)
        }.flowOn(Dispatchers.IO)
    }

    private suspend fun loadHolidays(cacheKey: HolidayCacheKey): List<Event> {
        val start = LocalDate.of(cacheKey.year, 1, 1)
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        val endExclusive = LocalDate.of(cacheKey.year + 1, 1, 1)
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        val local = publicHolidayDataStore
            .getHolidaysBetween(start, endExclusive, cacheKey.countryCode)
            .map { it.toDomainModel() }
        if (local.isNotEmpty()) {
            holidayMemoryCache[cacheKey] = local
            return local
        }

        return try {
            val entities = publicHolidayDataSource
                .getPublicHolidays(cacheKey.year, cacheKey.countryCode)
                .filter { it.global }
                .distinctBy { it.date to it.localName }
                .map { dto ->
                    EventEntity(
                        name = dto.localName,
                        description = dto.name,
                        date = LocalDate.parse(dto.date)
                            .atStartOfDay(ZoneId.systemDefault())
                            .toInstant()
                            .toEpochMilli(),
                        type = EventType.Holiday,
                        countryCode = cacheKey.countryCode
                    )
                }
            withContext(Dispatchers.IO) {
                publicHolidayDataStore.insertHolidays(entities)
            }
            entities.map { it.toDomainModel() }.also {
                holidayMemoryCache[cacheKey] = it
            }
        } catch (error: Exception) {
            if (error is CancellationException) throw error
            // Keep custom events usable offline, but do not cache failures so a later load can retry.
            emptyList()
        }
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

private data class HolidayCacheKey(val year: Int, val countryCode: String)
