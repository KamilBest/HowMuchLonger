package com.icyapps.howmuchlonger.data.repository

import com.icyapps.howmuchlonger.data.local.EventDataStore
import com.icyapps.howmuchlonger.data.model.EventEntity
import com.icyapps.howmuchlonger.data.model.PublicHolidayDto
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
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
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
        every { eventDataStore.getAllEvents() } returns flowOf(emptyList())
        coEvery { publicHolidayDataStore.getHolidaysBetween(any(), any(), "PL") } returns listOf(holidayEntity)
        val flow = repository.getAllEvents(2023, "PL", true)
        val result = flow.first()
        assertEquals(listOf(holidayEntity.toDomainModel()), result)
        coVerify(exactly = 0) { publicHolidayDataSource.getPublicHolidays(any(), any()) }
    }

    @Test
    fun `getHolidays fetches from API and caches if not present`() = runTest {
        every { eventDataStore.getAllEvents() } returns flowOf(emptyList())
        coEvery { publicHolidayDataStore.getHolidaysBetween(any(), any(), "PL") } returns emptyList()
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
    fun `getHolidays calls API only once for repeated year requests`() = runTest {
        every { eventDataStore.getAllEvents() } returns flowOf(emptyList())
        coEvery { publicHolidayDataStore.getHolidaysBetween(any(), any(), "PL") } returns emptyList()
        coEvery { publicHolidayDataSource.getPublicHolidays(2027, "PL") } returns listOf(
            com.icyapps.howmuchlonger.data.model.PublicHolidayDto(
                date = "2027-01-01",
                localName = "Nowy Rok",
                name = "New Year's Day",
                countryCode = "PL",
                fixed = true,
                global = true,
                counties = null,
                launchYear = null,
                types = listOf("Public")
            )
        )
        coEvery { publicHolidayDataStore.insertHolidays(any()) } returns Unit

        repository.getAllEvents(2027, "PL", true).first()
        repository.getAllEvents(2027, "PL", true).first()

        coVerify(exactly = 1) { publicHolidayDataSource.getPublicHolidays(2027, "PL") }
        coVerify(exactly = 1) { publicHolidayDataStore.insertHolidays(any()) }
    }

    @Test
    fun `concurrent holiday requests share one API call`() = runTest {
        val apiStarted = CompletableDeferred<Unit>()
        val releaseApi = CompletableDeferred<Unit>()
        every { eventDataStore.getAllEvents() } returns flowOf(emptyList())
        coEvery { publicHolidayDataStore.getHolidaysBetween(any(), any(), "PL") } returns emptyList()
        coEvery { publicHolidayDataSource.getPublicHolidays(2027, "PL") } coAnswers {
            apiStarted.complete(Unit)
            releaseApi.await()
            emptyList()
        }
        coEvery { publicHolidayDataStore.insertHolidays(any()) } returns Unit

        val firstRequest = async { repository.getAllEvents(2027, "PL", true).first() }
        apiStarted.await()
        val secondRequest = async { repository.getAllEvents(2027, "PL", true).first() }
        yield()
        releaseApi.complete(Unit)
        firstRequest.await()
        secondRequest.await()

        coVerify(exactly = 1) { publicHolidayDataSource.getPublicHolidays(2027, "PL") }
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
    fun `getHolidays keeps custom events available when API fails`() = runTest {
        every { eventDataStore.getAllEvents() } returns flowOf(emptyList())
        coEvery { publicHolidayDataStore.getHolidaysBetween(any(), any(), "PL") } returns emptyList()
        coEvery { publicHolidayDataSource.getPublicHolidays(any(), any()) } throws RuntimeException("API error")
        assertTrue(repository.getAllEvents(2023, "PL", true).first().isEmpty())
    }

    @Test
    fun `getAllEvents without holidays never reads holiday sources`() = runTest {
        every { eventDataStore.getAllEvents() } returns flowOf(listOf(testEventEntity))

        val result = repository.getAllEvents(2027, "PL", false).first()

        assertEquals(listOf(testEvent), result)
        coVerify(exactly = 0) { publicHolidayDataStore.getHolidaysBetween(any(), any(), any()) }
        coVerify(exactly = 0) { publicHolidayDataSource.getPublicHolidays(any(), any()) }
    }

    @Test
    fun `country code is normalized to uppercase`() = runTest {
        every { eventDataStore.getAllEvents() } returns flowOf(emptyList())
        coEvery { publicHolidayDataStore.getHolidaysBetween(any(), any(), "PL") } returns emptyList()
        coEvery { publicHolidayDataSource.getPublicHolidays(2027, "PL") } returns emptyList()
        coEvery { publicHolidayDataStore.insertHolidays(any()) } returns Unit

        repository.getAllEvents(2027, "pl", true).first()

        coVerify(exactly = 1) { publicHolidayDataSource.getPublicHolidays(2027, "PL") }
    }

    @Test
    fun `invalid country code falls back to Poland`() = runTest {
        every { eventDataStore.getAllEvents() } returns flowOf(emptyList())
        coEvery { publicHolidayDataStore.getHolidaysBetween(any(), any(), "PL") } returns emptyList()
        coEvery { publicHolidayDataSource.getPublicHolidays(2027, "PL") } returns emptyList()
        coEvery { publicHolidayDataStore.insertHolidays(any()) } returns Unit

        repository.getAllEvents(2027, "POL", true).first()

        coVerify(exactly = 1) { publicHolidayDataSource.getPublicHolidays(2027, "PL") }
    }

    @Test
    fun `regional holidays are excluded from API result`() = runTest {
        every { eventDataStore.getAllEvents() } returns flowOf(emptyList())
        coEvery { publicHolidayDataStore.getHolidaysBetween(any(), any(), "PL") } returns emptyList()
        coEvery { publicHolidayDataSource.getPublicHolidays(2027, "PL") } returns listOf(
            holidayDto("2027-01-01", "Global holiday", global = true),
            holidayDto("2027-02-01", "Regional holiday", global = false)
        )
        coEvery { publicHolidayDataStore.insertHolidays(any()) } returns Unit

        val result = repository.getAllEvents(2027, "PL", true).first()

        assertEquals(listOf("Global holiday"), result.map { it.name })
        coVerify {
            publicHolidayDataStore.insertHolidays(match { entities ->
                entities.size == 1 && entities.single().name == "Global holiday"
            })
        }
    }

    @Test
    fun `duplicate holidays from API are stored once`() = runTest {
        every { eventDataStore.getAllEvents() } returns flowOf(emptyList())
        coEvery { publicHolidayDataStore.getHolidaysBetween(any(), any(), "PL") } returns emptyList()
        val duplicate = holidayDto("2027-01-01", "Nowy Rok")
        coEvery { publicHolidayDataSource.getPublicHolidays(2027, "PL") } returns listOf(duplicate, duplicate)
        coEvery { publicHolidayDataStore.insertHolidays(any()) } returns Unit

        val result = repository.getAllEvents(2027, "PL", true).first()

        assertEquals(1, result.size)
        coVerify { publicHolidayDataStore.insertHolidays(match { it.size == 1 }) }
    }

    @Test
    fun `API failure is not cached and next request retries`() = runTest {
        every { eventDataStore.getAllEvents() } returns flowOf(emptyList())
        coEvery { publicHolidayDataStore.getHolidaysBetween(any(), any(), "PL") } returns emptyList()
        coEvery { publicHolidayDataSource.getPublicHolidays(2027, "PL") } throws
            RuntimeException("offline") andThen listOf(holidayDto("2027-01-01", "Nowy Rok"))
        coEvery { publicHolidayDataStore.insertHolidays(any()) } returns Unit

        val first = repository.getAllEvents(2027, "PL", true).first()
        val second = repository.getAllEvents(2027, "PL", true).first()

        assertTrue(first.isEmpty())
        assertEquals(listOf("Nowy Rok"), second.map { it.name })
        coVerify(exactly = 2) { publicHolidayDataSource.getPublicHolidays(2027, "PL") }
    }

    @Test
    fun `holiday cache is isolated by year and country`() = runTest {
        every { eventDataStore.getAllEvents() } returns flowOf(emptyList())
        coEvery { publicHolidayDataStore.getHolidaysBetween(any(), any(), any()) } returns emptyList()
        coEvery { publicHolidayDataSource.getPublicHolidays(any(), any()) } returns emptyList()
        coEvery { publicHolidayDataStore.insertHolidays(any()) } returns Unit

        repository.getAllEvents(2027, "PL", true).first()
        repository.getAllEvents(2028, "PL", true).first()
        repository.getAllEvents(2027, "DE", true).first()

        coVerify(exactly = 1) { publicHolidayDataSource.getPublicHolidays(2027, "PL") }
        coVerify(exactly = 1) { publicHolidayDataSource.getPublicHolidays(2028, "PL") }
        coVerify(exactly = 1) { publicHolidayDataSource.getPublicHolidays(2027, "DE") }
    }

    @Test
    fun `custom events and holidays are merged chronologically`() = runTest {
        val laterCustom = testEventEntity.copy(date = 1_800_000_000_000L)
        val earlierHoliday = testEventEntity.copy(
            id = 2L,
            name = "Holiday",
            date = 1_700_000_000_000L,
            type = EventType.Holiday,
            countryCode = "PL"
        )
        every { eventDataStore.getAllEvents() } returns flowOf(listOf(laterCustom))
        coEvery { publicHolidayDataStore.getHolidaysBetween(any(), any(), "PL") } returns listOf(earlierHoliday)

        val result = repository.getAllEvents(2027, "PL", true).first()

        assertEquals(listOf("Holiday", "Test Event"), result.map { it.name })
    }

    private fun holidayDto(
        date: String,
        localName: String,
        global: Boolean = true
    ) = PublicHolidayDto(
        date = date,
        localName = localName,
        name = "$localName EN",
        countryCode = "PL",
        fixed = true,
        global = global,
        counties = null,
        launchYear = null,
        types = listOf("Public")
    )
}
