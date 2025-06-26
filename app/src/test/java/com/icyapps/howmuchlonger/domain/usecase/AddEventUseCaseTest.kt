package com.icyapps.howmuchlonger.domain.usecase

import com.icyapps.howmuchlonger.domain.model.Event
import com.icyapps.howmuchlonger.domain.repository.EventRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class AddEventUseCaseTest {

    private lateinit var repository: EventRepository
    private lateinit var addEventUseCase: AddEventUseCase

    @Before
    fun setup() {
        repository = mockk()
        addEventUseCase = AddEventUseCase(repository)
    }

    @Test
    fun `invoke creates event with correct parameters and returns id`() = runTest {
        // Given
        val name = "Test Event"
        val description = "Test Description"
        val date = 1672531200000L // 2023-01-01
        val expectedId = 1L

        coEvery { 
            repository.insertEvent(match { 
                it.name == name && 
                it.description == description && 
                it.date == date 
            }) 
        } returns expectedId

        // When
        val result = addEventUseCase(name, description, date)

        // Then
        assertEquals(expectedId, result)
    }
}
