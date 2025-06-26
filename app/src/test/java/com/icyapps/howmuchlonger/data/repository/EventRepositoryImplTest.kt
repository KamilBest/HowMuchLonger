package com.icyapps.howmuchlonger.data.repository

import com.icyapps.howmuchlonger.data.local.EventDao
import com.icyapps.howmuchlonger.data.model.EventEntity
import com.icyapps.howmuchlonger.data.model.toDomainModel
import com.icyapps.howmuchlonger.domain.model.Event
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class EventRepositoryImplTest {

    private lateinit var eventDao: EventDao
    private lateinit var repository: EventRepositoryImpl

    private val testEvent = Event(
        id = 1L,
        name = "Test Event",
        description = "Test Description",
        date = 1672531200000 // 2023-01-01
    )

    private val testEventEntity = EventEntity(
        id = 1L,
        name = "Test Event",
        description = "Test Description",
        date = 1672531200000 // 2023-01-01
    )

    private val testEventsList = listOf(
        testEventEntity,
        EventEntity(
            id = 2L,
            name = "Test Event 2",
            description = "Test Description 2",
            date = 1675209600000 // 2023-02-01
        )
    )

    @Before
    fun setup() {
        eventDao = mockk()
        repository = EventRepositoryImpl(eventDao)
    }

    @Test
    fun `getAllEvents returns mapped domain models`() = runTest {
        // Given
        every { eventDao.getAllEvents() } returns flowOf(testEventsList)

        // When
        val result = repository.getAllEvents().first()

        // Then
        assertEquals(2, result.size)
        assertEquals(testEventsList[0].toDomainModel(), result[0])
        assertEquals(testEventsList[1].toDomainModel(), result[1])
    }

    @Test
    fun `getEventById returns mapped domain model when event exists`() = runTest {
        // Given
        coEvery { eventDao.getEventById(1L) } returns testEventEntity

        // When
        val result = repository.getEventById(1L)

        // Then
        assertEquals(testEventEntity.toDomainModel(), result)
    }

    @Test
    fun `getEventById returns null when event does not exist`() = runTest {
        // Given
        coEvery { eventDao.getEventById(999L) } returns null

        // When
        val result = repository.getEventById(999L)

        // Then
        assertNull(result)
    }

    @Test
    fun `insertEvent calls dao and returns id`() = runTest {
        // Given
        val eventSlot = slot<EventEntity>()
        coEvery { eventDao.insertEvent(capture(eventSlot)) } returns 1L

        // When
        val result = repository.insertEvent(testEvent)

        // Then
        assertEquals(1L, result)
        assertEquals(testEvent.name, eventSlot.captured.name)
        assertEquals(testEvent.description, eventSlot.captured.description)
        assertEquals(testEvent.date, eventSlot.captured.date)
    }

    @Test
    fun `updateEvent calls dao with correct entity`() = runTest {
        // Given
        val eventSlot = slot<EventEntity>()
        coEvery { eventDao.updateEvent(capture(eventSlot)) } returns Unit

        // When
        repository.updateEvent(testEvent)

        // Then
        assertEquals(testEvent.id, eventSlot.captured.id)
        assertEquals(testEvent.name, eventSlot.captured.name)
        assertEquals(testEvent.description, eventSlot.captured.description)
        assertEquals(testEvent.date, eventSlot.captured.date)
    }

    @Test
    fun `deleteEvent calls dao with correct entity`() = runTest {
        // Given
        val eventSlot = slot<EventEntity>()
        coEvery { eventDao.deleteEvent(capture(eventSlot)) } returns Unit

        // When
        repository.deleteEvent(testEvent)

        // Then
        assertEquals(testEvent.id, eventSlot.captured.id)
        assertEquals(testEvent.name, eventSlot.captured.name)
        assertEquals(testEvent.description, eventSlot.captured.description)
        assertEquals(testEvent.date, eventSlot.captured.date)
    }

    @Test
    fun `getTop3Events returns mapped domain models`() = runTest {
        // Given
        every { eventDao.getTop3Events() } returns flowOf(testEventsList)

        // When
        val result = repository.getTop3Events().first()

        // Then
        assertEquals(2, result.size)
        assertEquals(testEventsList[0].toDomainModel(), result[0])
        assertEquals(testEventsList[1].toDomainModel(), result[1])
    }
}