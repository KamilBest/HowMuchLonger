package com.icyapps.howmuchlonger.data.store

import com.icyapps.howmuchlonger.data.local.EventDao
import com.icyapps.howmuchlonger.data.model.EventEntity
import com.icyapps.howmuchlonger.data.store.PublicHolidayRoomDataStore
import com.icyapps.howmuchlonger.domain.model.EventType
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test

class PublicHolidayRoomDataStoreTest {
    private lateinit var eventDao: EventDao
    private lateinit var dataStore: PublicHolidayRoomDataStore
    private val holiday = EventEntity(1L, "Holiday", "Desc", 123L, EventType.Holiday)
    private val normal = EventEntity(2L, "Normal", "Desc", 456L, EventType.Normal)

    @Before
    fun setup() {
        eventDao = mockk()
        dataStore = PublicHolidayRoomDataStore(eventDao)
    }

    @Test
    fun `getHolidaysBetween delegates country-scoped query`() = runTest {
        coEvery { eventDao.getHolidaysBetween(0L, 1000L, "PL") } returns listOf(holiday)
        val result = dataStore.getHolidaysBetween(0L, 1000L, "PL")
        assertEquals(listOf(holiday), result)
    }

    @Test
    fun `insertHolidays inserts all holidays`() = runTest {
        coEvery { eventDao.insertEvents(any()) } returns Unit
        dataStore.insertHolidays(listOf(holiday, normal))
        coVerify { eventDao.insertEvents(listOf(holiday, normal)) }
    }

    @Test
    fun `insertHolidays with empty list does not call DAO`() = runTest {
        dataStore.insertHolidays(emptyList())
        // No call to eventDao.insertEvent should be made
        // (mockk will throw if called unexpectedly)
    }

    @Test
    fun `getHolidaysBetween propagates DAO error`() = runTest {
        coEvery { eventDao.getHolidaysBetween(any(), any(), any()) } throws RuntimeException("DAO error")
        assertThrows(RuntimeException::class.java) {
            runTest { dataStore.getHolidaysBetween(0L, 1000L, "PL") }
        }
    }

    @Test
    fun `insertHolidays propagates DAO error`() = runTest {
        val holiday = EventEntity(1L, "Holiday", "Desc", 123L, EventType.Holiday)
        coEvery { eventDao.insertEvents(listOf(holiday)) } throws RuntimeException("DAO error")
        assertThrows(RuntimeException::class.java) {
            runTest { dataStore.insertHolidays(listOf(holiday)) }
        }
    }

    @Test
    fun `insertHolidays with edge values`() = runTest {
        val edgeHoliday = EventEntity(0L, "", "", 0L, EventType.Holiday)
        coEvery { eventDao.insertEvents(listOf(edgeHoliday)) } returns Unit
        dataStore.insertHolidays(listOf(edgeHoliday))
        coVerify { eventDao.insertEvents(listOf(edgeHoliday)) }
    }
}
