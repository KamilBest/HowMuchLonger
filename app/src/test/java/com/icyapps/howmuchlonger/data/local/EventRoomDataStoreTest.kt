package com.icyapps.howmuchlonger.data.local

import com.icyapps.howmuchlonger.data.model.EventEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test

class EventRoomDataStoreTest {
    private lateinit var eventDao: EventDao
    private lateinit var dataStore: EventRoomDataStore
    private val testEvent = EventEntity(1L, "Test", "Desc", 123L)

    @Before
    fun setup() {
        eventDao = mockk()
        dataStore = EventRoomDataStore(eventDao)
    }

    @Test
    fun `insertEvent propagates DAO error for duplicate`() = runTest {
        coEvery { eventDao.insertEvent(testEvent) } throws RuntimeException("Duplicate")
        assertThrows(RuntimeException::class.java) {
            runTest { dataStore.insertEvent(testEvent) }
        }
    }

    @Test
    fun `updateEvent propagates DAO error for non-existent event`() = runTest {
        coEvery { eventDao.updateEvent(testEvent) } throws RuntimeException("Not found")
        assertThrows(RuntimeException::class.java) {
            runTest { dataStore.updateEvent(testEvent) }
        }
    }

    @Test
    fun `deleteEvent propagates DAO error for non-existent event`() = runTest {
        coEvery { eventDao.deleteEvent(testEvent) } throws RuntimeException("Not found")
        assertThrows(RuntimeException::class.java) {
            runTest { dataStore.deleteEvent(testEvent) }
        }
    }

    @Test
    fun `insertEvent with empty values`() = runTest {
        val emptyEvent = EventEntity(0L, "", "", 0L)
        coEvery { eventDao.insertEvent(emptyEvent) } returns 2L
        val result = dataStore.insertEvent(emptyEvent)
        assertEquals(2L, result)
    }

    @Test
    fun `updateEvent with empty values`() = runTest {
        val emptyEvent = EventEntity(0L, "", "", 0L)
        coEvery { eventDao.updateEvent(emptyEvent) } returns Unit
        dataStore.updateEvent(emptyEvent)
        coVerify { eventDao.updateEvent(emptyEvent) }
    }

    @Test
    fun `deleteEvent with empty values`() = runTest {
        val emptyEvent = EventEntity(0L, "", "", 0L)
        coEvery { eventDao.deleteEvent(emptyEvent) } returns Unit
        dataStore.deleteEvent(emptyEvent)
        coVerify { eventDao.deleteEvent(emptyEvent) }
    }
}
