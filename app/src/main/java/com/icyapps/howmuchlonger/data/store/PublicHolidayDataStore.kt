package com.icyapps.howmuchlonger.data.store

import com.icyapps.howmuchlonger.data.model.EventEntity
import com.icyapps.howmuchlonger.data.local.EventDao

interface PublicHolidayDataStore {
    suspend fun getHolidaysBetween(start: Long, end: Long): List<EventEntity>
    suspend fun insertHolidays(holidays: List<EventEntity>)
}

class PublicHolidayRoomDataStore(private val eventDao: EventDao) : PublicHolidayDataStore {
    override suspend fun getHolidaysBetween(start: Long, end: Long): List<EventEntity> {
        return eventDao.getEventsBetween(start, end).filter { it.type == com.icyapps.howmuchlonger.domain.model.EventType.Holiday }
    }
    override suspend fun insertHolidays(holidays: List<EventEntity>) {
        holidays.forEach { eventDao.insertEvent(it) }
    }
} 