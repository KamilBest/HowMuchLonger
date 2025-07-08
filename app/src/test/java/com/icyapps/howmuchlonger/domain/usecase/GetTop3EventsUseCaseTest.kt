package com.icyapps.howmuchlonger.domain.usecase

import com.icyapps.howmuchlonger.domain.model.Event
import com.icyapps.howmuchlonger.domain.repository.EventRepository
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import io.mockk.coEvery
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test

class GetTop3EventsUseCaseTest {
    private lateinit var repository: EventRepository
    private lateinit var useCase: GetTop3EventsUseCase
    private val events = listOf(Event(1L, "A", "B", 123L))

    @Before
    fun setup() {
        repository = mockk()
        useCase = GetTop3EventsUseCase(repository)
    }

    @Test
    fun `invoke returns top 3 events from repository`() = runTest {
        coEvery { repository.getTop3Events() } returns flowOf(events)
        val result = useCase.invoke().first()
        assertEquals(events, result)
    }

    @Test
    fun `invoke throws exception if repository fails`() = runTest {
        coEvery { repository.getTop3Events() } throws RuntimeException("Repo error")
        assertThrows(RuntimeException::class.java) {
            runTest { useCase.invoke().first() }
        }
    }

    @Test
    fun `invoke returns empty list if repository returns empty`() = runTest {
        coEvery { repository.getTop3Events() } returns flowOf(emptyList())
        val result = useCase.invoke().first()
        assertEquals(emptyList<Event>(), result)
    }
} 