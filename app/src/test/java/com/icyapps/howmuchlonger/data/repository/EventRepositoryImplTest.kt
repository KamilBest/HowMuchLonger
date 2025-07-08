package com.icyapps.howmuchlonger.data.repository

import com.icyapps.howmuchlonger.data.local.EventDataStore
import com.icyapps.howmuchlonger.data.model.EventEntity
import com.icyapps.howmuchlonger.data.model.toDomainModel
import com.icyapps.howmuchlonger.domain.model.Event
import com.icyapps.howmuchlonger.domain.model.EventType
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
import com.icyapps.howmuchlonger.data.source.PublicHolidayDataSource
import com.icyapps.howmuchlonger.data.store.PublicHolidayDataStore
import org.junit.Assert.assertTrue
import org.junit.Assert.fail

class EventRepositoryImplTest {

    private lateinit var eventDataStore: EventDataStore
    private lateinit var publicHolidayDataSource: PublicHolidayDataSource
    private lateinit var publicHolidayDataStore: PublicHolidayDataStore
    private lateinit var repository: EventRepositoryImpl

    private val testEvent = Event(
        id = 1L,
        name = "Test Event",
        description = "Test Description",
        date = 1672531200000, // 2023-01-01
        type = EventType.Normal
    )

    private val testEventEntity = EventEntity(
        id = 1L,
        name = "Test Event",
        description = "Test Description",
        date = 1672531200000, // 2023-01-01
        type = EventType.Normal
    )

    private val testEventsList = listOf(
        testEventEntity,
        EventEntity(
            id = 2L,
            name = "Test Event 2",
            description = "Test Description 2",
            date = 1675209600000, // 2023-02-01
            type = EventType.Normal
        )
    )

    @Before
    fun setup() {
        eventDataStore = mockk()
        publicHolidayDataSource = mockk()
        publicHolidayDataStore = mockk()
        repository = EventRepositoryImpl(eventDataStore, publicHolidayDataSource, publicHolidayDataStore)
    }

    @Test
    fun `getAllEvents returns mapped domain models`() = runTest {
        // Given
        every { eventDataStore.getAllEvents() } returns flowOf(testEventsList)

        // When
        val result = repository.getCustomEvents().first()

        // Then
        assertEquals(2, result.size)
        assertEquals(testEventsList[0].toDomainModel(), result[0])
        assertEquals(testEventsList[1].toDomainModel(), result[1])
    }

    @Test
    fun `getEventById returns mapped domain model when event exists`() = runTest {
        // Given
        coEvery { eventDataStore.getEventById(1L) } returns testEventEntity

        // When
        val result = repository.getEventById(1L)

        // Then
        assertEquals(testEventEntity.toDomainModel(), result)
    }

    @Test
    fun `getEventById returns null when event does not exist`() = runTest {
        // Given
        coEvery { eventDataStore.getEventById(999L) } returns null

        // When
        val result = repository.getEventById(999L)

        // Then
        assertNull(result)
    }

    @Test
    fun `insertEvent calls dataStore and returns id`() = runTest {
        // Given
        val eventSlot = slot<EventEntity>()
        coEvery { eventDataStore.insertEvent(capture(eventSlot)) } returns 1L

        // When
        val result = repository.insertEvent(testEvent)

        // Then
        assertEquals(1L, result)
        assertEquals(testEvent.name, eventSlot.captured.name)
        assertEquals(testEvent.description, eventSlot.captured.description)
        assertEquals(testEvent.date, eventSlot.captured.date)
    }

    @Test
    fun `updateEvent calls dataStore with correct entity`() = runTest {
        // Given
        val eventSlot = slot<EventEntity>()
        coEvery { eventDataStore.updateEvent(capture(eventSlot)) } returns Unit

        // When
        repository.updateEvent(testEvent)

        // Then
        assertEquals(testEvent.id, eventSlot.captured.id)
        assertEquals(testEvent.name, eventSlot.captured.name)
        assertEquals(testEvent.description, eventSlot.captured.description)
        assertEquals(testEvent.date, eventSlot.captured.date)
    }

    @Test
    fun `deleteEvent calls dataStore with correct entity`() = runTest {
        // Given
        val eventSlot = slot<EventEntity>()
        coEvery { eventDataStore.deleteEvent(capture(eventSlot)) } returns Unit

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
        every { eventDataStore.getTop3Events() } returns flowOf(testEventsList)

        // When
        val result = repository.getTop3Events().first()

        // Then
        assertEquals(2, result.size)
        assertEquals(testEventsList[0].toDomainModel(), result[0])
        assertEquals(testEventsList[1].toDomainModel(), result[1])
    }

    @Test
    fun `getHolidays returns cached holidays if present`() = runTest {
        val holidayEntity = testEventEntity.copy(type = EventType.Holiday)
        coEvery { publicHolidayDataStore.getHolidaysBetween(any(), any()) } returns listOf(holidayEntity)
        val flow = repository.getAllEvents(2023, "PL", true)
        val result = flow.first()
        assertEquals(listOf(holidayEntity.toDomainModel()), result)
    }

    @Test
    fun `getHolidays fetches from API and caches if not present`() = runTest {
        coEvery { publicHolidayDataStore.getHolidaysBetween(any(), any()) } returns emptyList() andThen listOf(testEventEntity.copy(type = EventType.Holiday))
        coEvery { publicHolidayDataSource.getPublicHolidays(any(), any()) } returns listOf(
            com.icyapps.howmuchlonger.data.model.PublicHolidayDto(
                date = "2023-01-01",
                localName = "Holiday",
                name = "Holiday Name",
                countryCode = "PL",
                fixed = true,
                global = true,
                counties = null,
                launchYear = 2023,
                types = listOf("Public")
            )
        )
        coEvery { publicHolidayDataStore.insertHolidays(any()) } returns Unit
        val flow = repository.getAllEvents(2023, "PL", true)
        val result = flow.first()
        assertEquals(EventType.Holiday, result.first().type)
    }

    @Test
    fun `getCustomEvents filters only custom events`() = runTest {
        every { eventDataStore.getAllEvents() } returns flowOf(listOf(
            testEventEntity.copy(type = EventType.Normal),
            testEventEntity.copy(type = EventType.Holiday)
        ))
        val result = repository.getCustomEvents().first()
        assertTrue(result.all { it.type == EventType.Normal })
    }

    @Test
    fun `getHolidayEvents filters only holiday events`() = runTest {
        every { eventDataStore.getAllEvents() } returns flowOf(listOf(
            testEventEntity.copy(type = EventType.Normal),
            testEventEntity.copy(type = EventType.Holiday)
        ))
        val result = repository.getHolidayEvents().first()
        assertTrue(result.all { it.type == EventType.Holiday })
    }

    @Test
    fun `getAllEvents returns empty list if no events`() = runTest {
        every { eventDataStore.getAllEvents() } returns flowOf(emptyList())
        val result = repository.getCustomEvents().first()
        assertTrue(result.isEmpty())
    }

    @Test
    fun `getAllEvents propagates data store error`() = runTest {
        every { eventDataStore.getAllEvents() } throws RuntimeException("DataStore error")
        try {
            repository.getCustomEvents().first()
            fail("Exception expected")
        } catch (e: Exception) {
            assertEquals("DataStore error", e.message)
        }
    }

    @Test
    fun `getHolidays propagates API error`() = runTest {
        coEvery { publicHolidayDataStore.getHolidaysBetween(any(), any()) } returns emptyList()
        coEvery { publicHolidayDataSource.getPublicHolidays(any(), any()) } throws RuntimeException("API error")
        try {
            repository.getAllEvents(2023, "PL", true).first()
            fail("Exception expected")
        } catch (e: Exception) {
            assertEquals("API error", e.message)
        }
    }
}