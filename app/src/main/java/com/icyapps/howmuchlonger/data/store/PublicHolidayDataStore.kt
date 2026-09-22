package com.icyapps.howmuchlonger.data.store

import com.icyapps.howmuchlonger.data.model.EventEntity
import com.icyapps.howmuchlonger.data.local.EventDao

interface PublicHolidayDataStore {
    suspend fun getHolidaysBetween(
        start: Long,
        endExclusive: Long,
        countryCode: String
    ): List<EventEntity>
    suspend fun insertHolidays(holidays: List<EventEntity>)
}

class PublicHolidayRoomDataStore(private val eventDao: EventDao) : PublicHolidayDataStore {
    override suspend fun getHolidaysBetween(
        start: Long,
        endExclusive: Long,
        countryCode: String
    ): List<EventEntity> {
        return eventDao.getHolidaysBetween(start, endExclusive, countryCode)
    }

    override suspend fun insertHolidays(holidays: List<EventEntity>) {
        if (holidays.isNotEmpty()) eventDao.insertEvents(holidays)
    }
}
