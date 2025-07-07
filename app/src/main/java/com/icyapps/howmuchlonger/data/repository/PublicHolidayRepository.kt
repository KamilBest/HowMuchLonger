package com.icyapps.howmuchlonger.data.repository

import com.icyapps.howmuchlonger.data.local.EventDao
import com.icyapps.howmuchlonger.data.model.EventEntity
import com.icyapps.howmuchlonger.data.model.PublicHolidayApi
import com.icyapps.howmuchlonger.domain.model.EventType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

class PublicHolidayRepository @Inject constructor(
    private val api: PublicHolidayApi,
    private val eventDao: EventDao
) {
    suspend fun fetchAndCacheHolidays(year: Int, countryCode: String) {
        val holidays = api.getPublicHolidays(year, countryCode)
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
        // Save to Room (replace or ignore duplicates as needed)
        withContext(Dispatchers.IO) {
            entities.forEach { eventDao.insertEvent(it) }
        }
    }

    suspend fun getHolidays(year: Int, countryCode: String): List<EventEntity> {
        // Try to get from local DB first
        val start =
            LocalDate.of(year, 1, 1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val end = LocalDate.of(year, 12, 31).atStartOfDay(ZoneId.systemDefault()).toInstant()
            .toEpochMilli()
        val local = eventDao.getEventsBetween(start, end).filter { it.type == EventType.Holiday }
        if (local.isNotEmpty()) return local
        // Otherwise fetch and cache
        fetchAndCacheHolidays(year, countryCode)
        return eventDao.getEventsBetween(start, end).filter { it.type == EventType.Holiday }
    }
} 