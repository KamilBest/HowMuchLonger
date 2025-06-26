package com.icyapps.howmuchlonger.domain.usecase

import com.icyapps.howmuchlonger.domain.model.Event
import com.icyapps.howmuchlonger.domain.repository.EventRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class UpdateEventUseCaseTest {

    private lateinit var repository: EventRepository
    private lateinit var updateEventUseCase: UpdateEventUseCase

    private val testEvent = Event(
        id = 1L,
        name = "Test Event",
        description = "Test Description",
        date = 1672531200000 // 2023-01-01
    )

    @Before
    fun setup() {
        repository = mockk()
        updateEventUseCase = UpdateEventUseCase(repository)
    }

    @Test
    fun `invoke calls repository updateEvent with correct event`() = runTest {
        // Given
        coEvery { repository.updateEvent(any()) } returns Unit

        // When
        updateEventUseCase(testEvent)

        // Then
        coVerify { repository.updateEvent(testEvent) }
    }
}