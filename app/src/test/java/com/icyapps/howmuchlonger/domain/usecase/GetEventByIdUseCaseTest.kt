package com.icyapps.howmuchlonger.domain.usecase

import com.icyapps.howmuchlonger.domain.model.Event
import com.icyapps.howmuchlonger.domain.repository.EventRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class GetEventByIdUseCaseTest {

    private lateinit var repository: EventRepository
    private lateinit var getEventByIdUseCase: GetEventByIdUseCase

    private val testEvent = Event(
        id = 1L,
        name = "Test Event",
        description = "Test Description",
        date = 1672531200000 // 2023-01-01
    )

    @Before
    fun setup() {
        repository = mockk()
        getEventByIdUseCase = GetEventByIdUseCase(repository)
    }

    @Test
    fun `invoke returns event when it exists`() = runTest {
        // Given
        val eventId = 1L
        coEvery { repository.getEventById(eventId) } returns testEvent

        // When
        val result = getEventByIdUseCase(eventId)

        // Then
        assertEquals(testEvent, result)
    }

    @Test
    fun `invoke returns null when event does not exist`() = runTest {
        // Given
        val eventId = 999L
        coEvery { repository.getEventById(eventId) } returns null

        // When
        val result = getEventByIdUseCase(eventId)

        // Then
        assertNull(result)
    }
}