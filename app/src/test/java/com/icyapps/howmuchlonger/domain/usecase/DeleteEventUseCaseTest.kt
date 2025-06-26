package com.icyapps.howmuchlonger.domain.usecase

import com.icyapps.howmuchlonger.domain.model.Event
import com.icyapps.howmuchlonger.domain.repository.EventRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class DeleteEventUseCaseTest {

    private lateinit var repository: EventRepository
    private lateinit var getEventByIdUseCase: GetEventByIdUseCase
    private lateinit var deleteEventUseCase: DeleteEventUseCase

    private val testEvent = Event(
        id = 1L,
        name = "Test Event",
        description = "Test Description",
        date = 1672531200000 // 2023-01-01
    )

    @Before
    fun setup() {
        repository = mockk()
        getEventByIdUseCase = mockk()
        deleteEventUseCase = DeleteEventUseCase(repository, getEventByIdUseCase)
    }

    @Test
    fun `invoke calls repository deleteEvent when event exists`() = runTest {
        // Given
        val eventId = 1L
        coEvery { getEventByIdUseCase(eventId) } returns testEvent
        coEvery { repository.deleteEvent(any()) } returns Unit

        // When
        deleteEventUseCase(eventId)

        // Then
        coVerify { repository.deleteEvent(testEvent) }
    }

    @Test
    fun `invoke does not call repository deleteEvent when event does not exist`() = runTest {
        // Given
        val eventId = 999L
        coEvery { getEventByIdUseCase(eventId) } returns null

        // When
        deleteEventUseCase(eventId)

        // Then
        coVerify(exactly = 0) { repository.deleteEvent(any()) }
    }
}
