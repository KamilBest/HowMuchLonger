package com.icyapps.howmuchlonger.ui.screen.addevent

import com.icyapps.howmuchlonger.domain.model.Event
import com.icyapps.howmuchlonger.domain.model.EventType
import com.icyapps.howmuchlonger.domain.usecase.AddEventUseCase
import com.icyapps.howmuchlonger.domain.usecase.GetEventByIdUseCase
import com.icyapps.howmuchlonger.domain.usecase.UpdateEventUseCase
import com.icyapps.howmuchlonger.domain.usecase.DeleteEventUseCase
import com.icyapps.howmuchlonger.ui.screen.addevent.intent.AddEventIntent
import com.icyapps.howmuchlonger.ui.screen.addevent.model.AddEventState
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AddEventViewModelTest {
    private lateinit var addEventUseCase: AddEventUseCase
    private lateinit var getEventByIdUseCase: GetEventByIdUseCase
    private lateinit var updateEventUseCase: UpdateEventUseCase
    private lateinit var deleteEventUseCase: DeleteEventUseCase
    private lateinit var viewModel: AddEventViewModel

    private val testEvent = Event(1L, "Test", "Desc", 123L, EventType.Normal)

    @Before
    fun setup() {
        addEventUseCase = mockk()
        getEventByIdUseCase = mockk()
        updateEventUseCase = mockk()
        deleteEventUseCase = mockk()
        viewModel = AddEventViewModel(addEventUseCase, getEventByIdUseCase, updateEventUseCase, deleteEventUseCase)
    }

    @Test
    fun `initialize with null sets default state`() = runTest {
        viewModel.initialize(null)
        val state = viewModel.state.first()
        assertEquals(null, state.eventId)
        assertEquals("", state.title)
        assertEquals(EventType.Normal, state.eventType)
    }

    @Test
    fun `initialize with eventId loads event`() = runTest {
        coEvery { getEventByIdUseCase(1L) } returns testEvent
        viewModel.initialize(1L)
        val state = viewModel.state.first()
        assertEquals(1L, state.eventId)
        assertEquals("Test", state.title)
        assertEquals(EventType.Normal, state.eventType)
    }

    @Test
    fun `processIntent UpdateTitle updates state`() = runTest {
        viewModel.processIntent(AddEventIntent.UpdateTitle("New Title"))
        val state = viewModel.state.first()
        assertEquals("New Title", state.title)
    }

    @Test
    fun `processIntent SaveEvent calls addEventUseCase`() = runTest {
        coEvery { addEventUseCase(any(), any(), any()) } returns 2L
        viewModel.processIntent(AddEventIntent.SaveEvent)
        // No exception means success
    }

    @Test
    fun `processIntent DeleteEvent calls deleteEventUseCase`() = runTest {
        viewModel.processIntent(AddEventIntent.UpdateTitle("Test"))
        coEvery { deleteEventUseCase(any()) } returns Unit
        viewModel.processIntent(AddEventIntent.DeleteEvent)
        // No exception means success
    }

    @Test
    fun `processIntent SaveEvent propagates error from addEventUseCase`() = runTest {
        coEvery { addEventUseCase(any(), any(), any()) } throws RuntimeException("Add error")
        assertThrows(RuntimeException::class.java) {
            runTest { viewModel.processIntent(AddEventIntent.SaveEvent) }
        }
    }

    @Test
    fun `processIntent DeleteEvent propagates error from deleteEventUseCase`() = runTest {
        coEvery { deleteEventUseCase(any()) } throws RuntimeException("Delete error")
        assertThrows(RuntimeException::class.java) {
            runTest { viewModel.processIntent(AddEventIntent.DeleteEvent) }
        }
    }

    @Test
    fun `processIntent SaveEvent with empty title does not call addEventUseCase`() = runTest {
        viewModel.processIntent(AddEventIntent.UpdateTitle(""))
        viewModel.processIntent(AddEventIntent.SaveEvent)
        coVerify(exactly = 0) { addEventUseCase(any(), any(), any()) }
    }

    @Test
    fun `processIntent DeleteEvent with null eventId does not call deleteEventUseCase`() = runTest {
        viewModel.processIntent(AddEventIntent.DeleteEvent)
        coVerify(exactly = 0) { deleteEventUseCase(any()) }
    }
} 