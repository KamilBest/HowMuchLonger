package com.icyapps.howmuchlonger.ui.screen.addevent

import com.icyapps.howmuchlonger.MainDispatcherRule
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalCoroutinesApi::class)
class AddEventViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

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
    fun `initialize new event uses date selected in calendar`() = runTest {
        val selectedDate = 1_809_792_000_000L

        viewModel.initialize(null, selectedDate)

        assertEquals(selectedDate, viewModel.state.value.date)
    }

    @Test
    fun `reinitializing new event after configuration change keeps form values`() = runTest {
        viewModel.initialize(null)
        viewModel.processIntent(AddEventIntent.UpdateTitle("Wpisana nazwa"))
        viewModel.processIntent(AddEventIntent.UpdateDescription("Wpisany opis"))

        viewModel.initialize(null)

        assertEquals("Wpisana nazwa", viewModel.state.value.title)
        assertEquals("Wpisany opis", viewModel.state.value.description)
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
    fun `reinitializing edited event after configuration change does not reload it`() = runTest {
        coEvery { getEventByIdUseCase(1L) } returns testEvent
        viewModel.initialize(1L)
        viewModel.processIntent(AddEventIntent.UpdateTitle("Lokalna zmiana"))

        viewModel.initialize(1L)

        assertEquals("Lokalna zmiana", viewModel.state.value.title)
        coVerify(exactly = 1) { getEventByIdUseCase(1L) }
    }

    @Test
    fun `processIntent UpdateTitle updates state`() = runTest {
        viewModel.processIntent(AddEventIntent.UpdateTitle("New Title"))
        val state = viewModel.state.first()
        assertEquals("New Title", state.title)
    }

    @Test
    fun `processIntent SaveEvent calls addEventUseCase`() = runTest {
        coEvery { addEventUseCase(any(), any(), any(), any()) } returns 2L
        viewModel.processIntent(AddEventIntent.UpdateTitle("Test"))
        viewModel.processIntent(AddEventIntent.SaveEvent)
        coVerify(exactly = 1) { addEventUseCase("Test", any(), any(), null) }
    }

    @Test
    fun `saving date range passes its end date to add event use case`() = runTest {
        val start = 1_800_000_000_000L
        val end = start + 3 * 86_400_000L
        coEvery { addEventUseCase(any(), any(), any(), any()) } returns 2L
        viewModel.processIntent(AddEventIntent.UpdateTitle("Holiday"))
        viewModel.processIntent(AddEventIntent.UpdateDateRange(start, end))

        viewModel.processIntent(AddEventIntent.SaveEvent)

        assertEquals(end, viewModel.state.value.endDate)
        assertEquals(false, viewModel.state.value.includeTime)
        coVerify(exactly = 1) { addEventUseCase("Holiday", any(), start, end) }
    }

    @Test
    fun `same day selection remains a single date`() = runTest {
        val selectedDate = 1_800_000_000_000L

        viewModel.processIntent(AddEventIntent.UpdateDateRange(selectedDate, selectedDate))

        assertEquals(selectedDate, viewModel.state.value.date)
        assertEquals(null, viewModel.state.value.endDate)
        assertEquals(true, viewModel.state.value.includeTime)
    }

    @Test
    fun `processIntent DeleteEvent calls deleteEventUseCase`() = runTest {
        coEvery { getEventByIdUseCase(1L) } returns testEvent
        coEvery { deleteEventUseCase(any()) } returns Unit
        viewModel.initialize(1L)
        viewModel.processIntent(AddEventIntent.DeleteEvent)
        coVerify(exactly = 1) { deleteEventUseCase(1L) }
    }

    @Test
    fun `processIntent SaveEvent propagates error from addEventUseCase`() = runTest {
        coEvery { addEventUseCase(any(), any(), any(), any()) } throws RuntimeException("Add error")
        viewModel.processIntent(AddEventIntent.UpdateTitle("Test"))
        viewModel.processIntent(AddEventIntent.SaveEvent)
        assertEquals("Add error", viewModel.state.value.error)
    }

    @Test
    fun `processIntent DeleteEvent propagates error from deleteEventUseCase`() = runTest {
        coEvery { getEventByIdUseCase(1L) } returns testEvent
        coEvery { deleteEventUseCase(any()) } throws RuntimeException("Delete error")
        viewModel.initialize(1L)
        viewModel.processIntent(AddEventIntent.DeleteEvent)
        assertEquals("Delete error", viewModel.state.value.error)
    }

    @Test
    fun `processIntent SaveEvent with empty title does not call addEventUseCase`() = runTest {
        viewModel.processIntent(AddEventIntent.UpdateTitle(""))
        viewModel.processIntent(AddEventIntent.SaveEvent)
        coVerify(exactly = 0) { addEventUseCase(any(), any(), any(), any()) }
    }

    @Test
    fun `processIntent DeleteEvent with null eventId does not call deleteEventUseCase`() = runTest {
        viewModel.processIntent(AddEventIntent.DeleteEvent)
        coVerify(exactly = 0) { deleteEventUseCase(any()) }
    }

    @Test
    fun `initialize loads description range and disables time for a range`() = runTest {
        val zone = ZoneId.systemDefault()
        val start = LocalDate.now().plusDays(10).atStartOfDay(zone).toInstant().toEpochMilli()
        val end = LocalDate.now().plusDays(12).atStartOfDay(zone).toInstant().toEpochMilli()
        val rangeEvent = testEvent.copy(date = start, endDate = end)
        coEvery { getEventByIdUseCase(rangeEvent.id) } returns rangeEvent

        viewModel.initialize(rangeEvent.id)

        val state = viewModel.state.value
        assertEquals(rangeEvent.description, state.description)
        assertEquals(rangeEvent.endDate, state.endDate)
        assertFalse(state.includeTime)
        assertFalse(state.isLoading)
    }

    @Test
    fun `initialize timed range exposes its start time`() = runTest {
        val zone = ZoneId.systemDefault()
        val start = LocalDate.now().plusDays(10).atTime(14, 30)
            .atZone(zone).toInstant().toEpochMilli()
        val end = LocalDate.now().plusDays(12).atStartOfDay(zone).toInstant().toEpochMilli()
        val rangeEvent = testEvent.copy(date = start, endDate = end)
        coEvery { getEventByIdUseCase(rangeEvent.id) } returns rangeEvent

        viewModel.initialize(rangeEvent.id)

        assertTrue(viewModel.state.value.includeTime)
        assertEquals(end, viewModel.state.value.endDate)
    }

    @Test
    fun `initialize reports missing event`() = runTest {
        coEvery { getEventByIdUseCase(404L) } returns null

        viewModel.initialize(404L)

        assertEquals("Event not found", viewModel.state.value.error)
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `initialize reports repository error`() = runTest {
        coEvery { getEventByIdUseCase(1L) } throws RuntimeException("Load error")

        viewModel.initialize(1L)

        assertEquals("Load error", viewModel.state.value.error)
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `description intent updates description`() = runTest {
        viewModel.processIntent(AddEventIntent.UpdateDescription("Szczegóły wyjazdu"))

        assertEquals("Szczegóły wyjazdu", viewModel.state.value.description)
    }

    @Test
    fun `end date before start is discarded`() = runTest {
        val start = 1_800_000_000_000L

        viewModel.processIntent(AddEventIntent.UpdateDateRange(start, start - 1))

        assertNull(viewModel.state.value.endDate)
        assertTrue(viewModel.state.value.includeTime)
    }

    @Test
    fun `disabling time normalizes selected date to local midnight`() = runTest {
        val zone = ZoneId.systemDefault()
        val selectedDate = LocalDate.now().plusDays(30)
        val timestamp = selectedDate.atTime(15, 47).atZone(zone).toInstant().toEpochMilli()
        viewModel.processIntent(AddEventIntent.UpdateDateRange(timestamp, null))

        viewModel.processIntent(AddEventIntent.ToggleIncludeTime(false))

        assertFalse(viewModel.state.value.includeTime)
        assertEquals(selectedDate.atStartOfDay(zone).toInstant().toEpochMilli(), viewModel.state.value.date)
    }

    @Test
    fun `enabling time keeps selected day and uses a full hour`() = runTest {
        val zone = ZoneId.systemDefault()
        val selectedDate = LocalDate.now().plusDays(30)
        val midnight = selectedDate.atStartOfDay(zone).toInstant().toEpochMilli()
        viewModel.processIntent(AddEventIntent.UpdateDateRange(midnight, null))
        viewModel.processIntent(AddEventIntent.ToggleIncludeTime(false))

        viewModel.processIntent(AddEventIntent.ToggleIncludeTime(true))

        val selectedDateTime = Instant.ofEpochMilli(viewModel.state.value.date).atZone(zone)
        assertTrue(viewModel.state.value.includeTime)
        assertEquals(selectedDate, selectedDateTime.toLocalDate())
        assertEquals(0, selectedDateTime.minute)
        assertEquals(0, selectedDateTime.second)
    }

    @Test
    fun `enabling time for a range keeps its end date`() = runTest {
        val zone = ZoneId.systemDefault()
        val startDate = LocalDate.now().plusDays(30)
        val endDate = startDate.plusDays(5).atStartOfDay(zone).toInstant().toEpochMilli()
        viewModel.processIntent(
            AddEventIntent.UpdateDateRange(
                startDate.atStartOfDay(zone).toInstant().toEpochMilli(),
                endDate
            )
        )

        viewModel.processIntent(AddEventIntent.ToggleIncludeTime(true))

        assertTrue(viewModel.state.value.includeTime)
        assertEquals(endDate, viewModel.state.value.endDate)
    }

    @Test
    fun `date and time picker intents update visibility`() = runTest {
        viewModel.processIntent(AddEventIntent.ShowDatePicker)
        assertTrue(viewModel.state.value.showDatePicker)
        viewModel.processIntent(AddEventIntent.HideDatePicker)
        assertFalse(viewModel.state.value.showDatePicker)

        viewModel.processIntent(AddEventIntent.ShowTimePicker)
        assertTrue(viewModel.state.value.showTimePicker)
        viewModel.processIntent(AddEventIntent.HideTimePicker)
        assertFalse(viewModel.state.value.showTimePicker)
    }

    @Test
    fun `saving an existing event updates all editable fields`() = runTest {
        val endDate = 1_900_259_200_000L
        coEvery { getEventByIdUseCase(testEvent.id) } returns testEvent
        coEvery { updateEventUseCase(any()) } returns Unit
        viewModel.initialize(testEvent.id)
        viewModel.processIntent(AddEventIntent.UpdateTitle("Nowa nazwa"))
        viewModel.processIntent(AddEventIntent.UpdateDescription("Nowy opis"))
        viewModel.processIntent(AddEventIntent.UpdateDateRange(1_900_000_000_000L, endDate))

        viewModel.processIntent(AddEventIntent.SaveEvent)

        coVerify(exactly = 1) {
            updateEventUseCase(match {
                it.id == testEvent.id &&
                    it.name == "Nowa nazwa" &&
                    it.description == "Nowy opis" &&
                    it.date == 1_900_000_000_000L &&
                    it.endDate == endDate
            })
        }
        assertTrue(viewModel.state.value.saveCompleted)
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `update failure is exposed and stops loading`() = runTest {
        coEvery { getEventByIdUseCase(testEvent.id) } returns testEvent
        coEvery { updateEventUseCase(any()) } throws RuntimeException("Update error")
        viewModel.initialize(testEvent.id)

        viewModel.processIntent(AddEventIntent.SaveEvent)

        assertEquals("Update error", viewModel.state.value.error)
        assertFalse(viewModel.state.value.isLoading)
        assertFalse(viewModel.state.value.saveCompleted)
    }

    @Test
    fun `holiday cannot be saved from edit state`() = runTest {
        val holiday = testEvent.copy(type = EventType.Holiday)
        coEvery { getEventByIdUseCase(holiday.id) } returns holiday
        viewModel.initialize(holiday.id)

        viewModel.processIntent(AddEventIntent.SaveEvent)

        coVerify(exactly = 0) { addEventUseCase(any(), any(), any(), any()) }
        coVerify(exactly = 0) { updateEventUseCase(any()) }
        assertFalse(viewModel.state.value.saveCompleted)
    }

    @Test
    fun `navigate back clears edited form state`() = runTest {
        viewModel.processIntent(AddEventIntent.UpdateTitle("Temporary"))
        viewModel.processIntent(AddEventIntent.UpdateDescription("Temporary description"))
        viewModel.processIntent(AddEventIntent.UpdateDateRange(1_800_000_000_000L, 1_800_086_400_000L))
        viewModel.processIntent(AddEventIntent.ShowDatePicker)

        viewModel.processIntent(AddEventIntent.NavigateBack)

        val state = viewModel.state.value
        assertEquals("", state.title)
        assertEquals("", state.description)
        assertNull(state.eventId)
        assertNull(state.endDate)
        assertTrue(state.includeTime)
        assertFalse(state.showDatePicker)
        assertFalse(state.saveCompleted)
    }
}
