package com.icyapps.howmuchlonger.data.repository

import com.icyapps.howmuchlonger.data.local.EventDao
import com.icyapps.howmuchlonger.data.local.EventRoomDataStore
import com.icyapps.howmuchlonger.data.model.EventEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class EventRoomDataStoreTest {
    private lateinit var eventDao: EventDao
    private lateinit var dataStore: EventRoomDataStore
    private val testEvent = EventEntity(1L, "Test", "Desc", 123L)
    private val testEvents = listOf(testEvent)

    @Before
    fun setup() {
        eventDao = mockk()
        dataStore = EventRoomDataStore(eventDao)
    }

    @Test
    fun `getAllEvents delegates to dao`() = runTest {
        every { eventDao.getAllEvents() } returns flowOf(testEvents)
        val result = dataStore.getAllEvents().first()
        assertEquals(testEvents, result)
    }

    @Test
    fun `getTop3Events delegates to dao`() = runTest {
        every { eventDao.getTop3Events() } returns flowOf(testEvents)
        val result = dataStore.getTop3Events().first()
        assertEquals(testEvents, result)
    }

    @Test
    fun `getEventById delegates to dao`() = runTest {
        coEvery { eventDao.getEventById(1L) } returns testEvent
        val result = dataStore.getEventById(1L)
        assertEquals(testEvent, result)
    }

    @Test
    fun `insertEvent delegates to dao`() = runTest {
        coEvery { eventDao.insertEvent(testEvent) } returns 1L
        val result = dataStore.insertEvent(testEvent)
        assertEquals(1L, result)
    }

    @Test
    fun `updateEvent delegates to dao`() = runTest {
        coEvery { eventDao.updateEvent(testEvent) } returns Unit
        dataStore.updateEvent(testEvent)
        coVerify { eventDao.updateEvent(testEvent) }
    }

    @Test
    fun `deleteEvent delegates to dao`() = runTest {
        coEvery { eventDao.deleteEvent(testEvent) } returns Unit
        dataStore.deleteEvent(testEvent)
        coVerify { eventDao.deleteEvent(testEvent) }
    }
} 