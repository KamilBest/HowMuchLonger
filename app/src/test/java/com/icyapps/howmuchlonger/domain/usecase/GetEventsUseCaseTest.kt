package com.icyapps.howmuchlonger.domain.usecase

import app.cash.turbine.test
import com.icyapps.howmuchlonger.domain.model.Event
import com.icyapps.howmuchlonger.domain.repository.EventRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GetEventsUseCaseTest {

    private lateinit var repository: EventRepository
    private lateinit var getEventsUseCase: GetEventsUseCase

    private val testEvents = listOf(
        Event(
            id = 1L,
            name = "Test Event 1",
            description = "Test Description 1",
            date = 1672531200000 // 2023-01-01
        ),
        Event(
            id = 2L,
            name = "Test Event 2",
            description = "Test Description 2",
            date = 1675209600000 // 2023-02-01
        )
    )

    @Before
    fun setup() {
        repository = mockk()
        getEventsUseCase = GetEventsUseCase(repository)
    }

    @Test
    fun `invoke returns flow of events from repository`() = runTest {
        // Given
        val year = 2023
        val countryCode = "US"
        coEvery { repository.getAllEvents(year, countryCode, true) } returns flowOf(testEvents)

        // When & Then
        getEventsUseCase(year, countryCode).test {
            val result = awaitItem()
            assertEquals(2, result.size)
            assertEquals(testEvents[0], result[0])
            assertEquals(testEvents[1], result[1])
            awaitComplete()
        }
    }

    @Test
    fun `invoke returns empty list when repository returns empty list`() = runTest {
        // Given
        val year = 2023
        val countryCode = "US"
        coEvery { repository.getAllEvents(year, countryCode, true) } returns flowOf(emptyList())

        // When & Then
        getEventsUseCase(year, countryCode).test {
            val result = awaitItem()
            assertEquals(0, result.size)
            awaitComplete()
        }
    }

    @Test
    fun `invoke with includeHolidays false calls repository correctly`() = runTest {
        // Given
        val year = 2023
        val countryCode = "US"
        val includeHolidays = false
        coEvery { repository.getAllEvents(year, countryCode, includeHolidays) } returns flowOf(testEvents)

        // When & Then
        getEventsUseCase(year, countryCode, includeHolidays).test {
            val result = awaitItem()
            assertEquals(2, result.size)
            awaitComplete()
        }
    }
}
