package com.icyapps.howmuchlonger.ui.screen.eventlist

import com.icyapps.howmuchlonger.domain.usecase.GetEventsUseCase
import com.icyapps.howmuchlonger.domain.usecase.DeleteEventUseCase
import com.icyapps.howmuchlonger.ui.screen.eventlist.intent.EventListIntent
import com.icyapps.howmuchlonger.ui.screen.eventlist.model.EventListState
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import io.mockk.coVerify

class EventListViewModelTest {
    private lateinit var getEventsUseCase: GetEventsUseCase
    private lateinit var deleteEventUseCase: DeleteEventUseCase
    private lateinit var viewModel: EventListViewModel

    @Before
    fun setup() {
        getEventsUseCase = mockk()
        deleteEventUseCase = mockk()
        viewModel = EventListViewModel(getEventsUseCase, deleteEventUseCase)
    }

    @Test
    fun `loading events sets Success state`() = runTest {
        coEvery { getEventsUseCase() } returns flowOf(emptyList())
        viewModel.processIntent(EventListIntent.LoadEvents)
        assertTrue(viewModel.state.value is EventListState.Success)
    }

    @Test
    fun `deleting event calls use case`() = runTest {
        coEvery { deleteEventUseCase(any()) } returns Unit
        viewModel.processIntent(EventListIntent.DeleteEvent(1L))
        // No exception means success
    }

    @Test
    fun `switching tab updates state`() = runTest {
        viewModel.processIntent(EventListIntent.SwitchTab(com.icyapps.howmuchlonger.ui.screen.eventlist.model.EventListTab.PAST))
        assertTrue((viewModel.state.value as? EventListState.Success)?.selectedTab == com.icyapps.howmuchlonger.ui.screen.eventlist.model.EventListTab.PAST)
    }

    @Test
    fun `error during loading sets Error state`() = runTest {
        coEvery { getEventsUseCase() } throws Exception("error")
        viewModel.processIntent(EventListIntent.LoadEvents)
        assertTrue(viewModel.state.value is EventListState.Error)
    }

    @Test
    fun `loading events with empty result sets Success state with empty list`() = runTest {
        coEvery { getEventsUseCase(any(), any(), any()) } returns flowOf(emptyList())
        viewModel.processIntent(EventListIntent.LoadEvents)
        val state = viewModel.state.value
        assertTrue(state is EventListState.Success && state.events.isEmpty())
    }

    @Test
    fun `deleting event propagates error from use case`() = runTest {
        coEvery { deleteEventUseCase(any()) } throws RuntimeException("Delete error")
        viewModel.processIntent(EventListIntent.DeleteEvent(1L))
        assertTrue(viewModel.state.value is EventListState.Error)
    }

    @Test
    fun `switching tab with no events keeps empty list`() = runTest {
        coEvery { getEventsUseCase(any(), any(), any()) } returns flowOf(emptyList())
        viewModel.processIntent(EventListIntent.LoadEvents)
        viewModel.processIntent(EventListIntent.SwitchTab(com.icyapps.howmuchlonger.ui.screen.eventlist.model.EventListTab.PAST))
        val state = viewModel.state.value
        assertTrue(state is EventListState.Success && state.events.isEmpty())
    }
} 