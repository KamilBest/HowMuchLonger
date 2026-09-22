package com.icyapps.howmuchlonger.ui.screen.eventlist

import com.icyapps.howmuchlonger.MainDispatcherRule
import com.icyapps.howmuchlonger.domain.usecase.GetEventsUseCase
import com.icyapps.howmuchlonger.domain.usecase.DeleteEventUseCase
import com.icyapps.howmuchlonger.ui.screen.eventlist.intent.EventListIntent
import com.icyapps.howmuchlonger.ui.screen.eventlist.model.EventListState
import com.icyapps.howmuchlonger.ui.screen.eventlist.model.EventListTab
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import io.mockk.coVerify
import com.icyapps.howmuchlonger.domain.model.Event
import com.icyapps.howmuchlonger.domain.model.EventType
import org.junit.Assert.assertFalse
import org.junit.Assert.assertEquals
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

class EventListViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

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
        coEvery { getEventsUseCase(any(), any(), any()) } returns flowOf(emptyList())
        viewModel.processIntent(EventListIntent.LoadEvents)
        assertTrue(viewModel.state.value is EventListState.Success)
    }

    @Test
    fun `deleting event calls use case`() = runTest {
        val event = Event(1L, "Event", "", System.currentTimeMillis() + 10_000)
        coEvery { getEventsUseCase(any(), any(), any()) } returns flowOf(listOf(event))
        coEvery { deleteEventUseCase(any()) } returns Unit
        viewModel.processIntent(EventListIntent.LoadEvents)
        viewModel.processIntent(EventListIntent.DeleteEvent(1L))
        coVerify { deleteEventUseCase(1L) }
    }

    @Test
    fun `switching tab updates state`() = runTest {
        coEvery { getEventsUseCase(any(), any(), any()) } returns flowOf(emptyList())
        viewModel.processIntent(EventListIntent.LoadEvents)
        viewModel.processIntent(EventListIntent.SwitchTab(com.icyapps.howmuchlonger.ui.screen.eventlist.model.EventListTab.PAST))
        assertTrue((viewModel.state.value as? EventListState.Success)?.selectedTab == com.icyapps.howmuchlonger.ui.screen.eventlist.model.EventListTab.PAST)
    }

    @Test
    fun `error during loading sets Error state`() = runTest {
        coEvery { getEventsUseCase(any(), any(), any()) } throws Exception("error")
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
        val event = Event(1L, "Event", "", System.currentTimeMillis() + 10_000)
        coEvery { getEventsUseCase(any(), any(), any()) } returns flowOf(listOf(event))
        coEvery { deleteEventUseCase(any()) } throws RuntimeException("Delete error")
        viewModel.processIntent(EventListIntent.LoadEvents)
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

    @Test
    fun `toggling holidays hides only holiday events`() = runTest {
        val normal = Event(1L, "Event", "", System.currentTimeMillis() + 10_000)
        val holiday = Event(2L, "Holiday", "", System.currentTimeMillis() + 20_000, EventType.Holiday)
        coEvery { getEventsUseCase(any(), any(), any()) } returns flowOf(listOf(normal, holiday))
        viewModel.processIntent(EventListIntent.LoadEvents)

        viewModel.processIntent(EventListIntent.ToggleHolidays)

        val state = viewModel.state.value as EventListState.Success
        assertFalse(state.includeHolidays)
        assertEquals(listOf(normal), state.events)
    }

    @Test
    fun `holiday cannot be deleted from list intent`() = runTest {
        val holiday = Event(2L, "Holiday", "", System.currentTimeMillis() + 20_000, EventType.Holiday)
        coEvery { getEventsUseCase(any(), any(), any()) } returns flowOf(listOf(holiday))
        viewModel.processIntent(EventListIntent.LoadEvents)

        viewModel.processIntent(EventListIntent.DeleteEvent(holiday.id))

        coVerify(exactly = 0) { deleteEventUseCase(any()) }
    }

    @Test
    fun `reloading does not expose coroutine cancellation as an error`() = runTest {
        val events = MutableSharedFlow<List<Event>>()
        coEvery { getEventsUseCase(any(), any(), any()) } returns events

        viewModel.processIntent(EventListIntent.LoadEvents)
        viewModel.processIntent(EventListIntent.LoadEvents)
        events.emit(emptyList())

        assertTrue(viewModel.state.value is EventListState.Success)
    }

    @Test
    fun `deleting event keeps currently selected tab`() = runTest {
        val event = Event(1L, "Past", "", System.currentTimeMillis() - 10_000)
        val events = MutableStateFlow(listOf(event))
        coEvery { getEventsUseCase(any(), any(), any()) } returns events
        coEvery { deleteEventUseCase(event.id) } coAnswers { events.value = emptyList() }
        viewModel.processIntent(EventListIntent.LoadEvents)
        viewModel.processIntent(EventListIntent.SwitchTab(com.icyapps.howmuchlonger.ui.screen.eventlist.model.EventListTab.PAST))

        viewModel.processIntent(EventListIntent.DeleteEvent(event.id))

        assertEquals(
            com.icyapps.howmuchlonger.ui.screen.eventlist.model.EventListTab.PAST,
            (viewModel.state.value as EventListState.Success).selectedTab
        )
    }

    @Test
    fun `calendar selection is stored in state`() = runTest {
        coEvery { getEventsUseCase(any(), any(), any()) } returns flowOf(emptyList())
        viewModel.processIntent(EventListIntent.LoadEvents)
        val selectedDate = LocalDate.of(2027, 5, 12)

        viewModel.processIntent(EventListIntent.ToggleCalendar)
        viewModel.processIntent(EventListIntent.SelectCalendarDate(selectedDate))

        val state = viewModel.state.value as EventListState.Success
        assertTrue(state.showCalendar)
        assertEquals(selectedDate, state.selectedCalendarDate)
    }

    @Test
    fun `loading events fetches current and next year holidays`() = runTest {
        coEvery { getEventsUseCase(any(), any(), any()) } returns flowOf(emptyList())
        viewModel.processIntent(EventListIntent.LoadEvents)

        coVerify { getEventsUseCase(LocalDate.now().year, any(), true) }
        coVerify { getEventsUseCase(LocalDate.now().year + 1, any(), true) }
    }

    @Test
    fun `upcoming includes next year holidays only through one year from today`() = runTest {
        val zone = ZoneId.systemDefault()
        val lastVisibleDate = LocalDate.now().plusYears(1)
        val visibleHoliday = Event(
            2L,
            "Visible holiday",
            "",
            lastVisibleDate.atStartOfDay(zone).toInstant().toEpochMilli(),
            EventType.Holiday
        )
        val laterHoliday = visibleHoliday.copy(
            id = 3L,
            name = "Later holiday",
            date = lastVisibleDate.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
        )
        coEvery { getEventsUseCase(LocalDate.now().year, any(), true) } returns flowOf(emptyList())
        coEvery { getEventsUseCase(LocalDate.now().year + 1, any(), true) } returns
            flowOf(listOf(visibleHoliday, laterHoliday))

        viewModel.processIntent(EventListIntent.LoadEvents)

        assertEquals(
            listOf(visibleHoliday),
            (viewModel.state.value as EventListState.Success).events
        )
    }

    @Test
    fun `holiday remains upcoming for its entire calendar day`() = runTest {
        val todayHoliday = Event(
            id = 4L,
            name = "Today's holiday",
            description = "",
            date = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
            type = EventType.Holiday
        )
        coEvery { getEventsUseCase(any(), any(), any()) } returns flowOf(listOf(todayHoliday))

        viewModel.processIntent(EventListIntent.LoadEvents)

        assertEquals(
            listOf(todayHoliday),
            (viewModel.state.value as EventListState.Success).events
        )
    }

    @Test
    fun `active date range remains in upcoming until its final day ends`() = runTest {
        val zone = ZoneId.systemDefault()
        val activeRange = Event(
            id = 10L,
            name = "Holiday",
            description = "",
            date = LocalDate.now().minusDays(2).atStartOfDay(zone).toInstant().toEpochMilli(),
            endDate = LocalDate.now().plusDays(2).atStartOfDay(zone).toInstant().toEpochMilli()
        )
        coEvery { getEventsUseCase(any(), any(), any()) } returns flowOf(listOf(activeRange))

        viewModel.processIntent(EventListIntent.LoadEvents)

        assertEquals(listOf(activeRange), (viewModel.state.value as EventListState.Success).events)
    }

    @Test
    fun `finished date range appears in past`() = runTest {
        val zone = ZoneId.systemDefault()
        val finishedRange = Event(
            id = 11L,
            name = "Finished holiday",
            description = "",
            date = LocalDate.now().minusDays(4).atStartOfDay(zone).toInstant().toEpochMilli(),
            endDate = LocalDate.now().minusDays(2).atStartOfDay(zone).toInstant().toEpochMilli()
        )
        coEvery { getEventsUseCase(any(), any(), any()) } returns flowOf(listOf(finishedRange))
        viewModel.processIntent(EventListIntent.LoadEvents)

        viewModel.processIntent(EventListIntent.SwitchTab(com.icyapps.howmuchlonger.ui.screen.eventlist.model.EventListTab.PAST))

        assertEquals(listOf(finishedRange), (viewModel.state.value as EventListState.Success).events)
    }

    @Test
    fun `calendar cannot move beyond one year from today`() = runTest {
        coEvery { getEventsUseCase(any(), any(), any()) } returns flowOf(emptyList())
        viewModel.processIntent(EventListIntent.LoadEvents)
        val initialMonth = (viewModel.state.value as EventListState.Success).calendarMonth

        viewModel.processIntent(EventListIntent.ChangeCalendarMonth(13))

        assertEquals(initialMonth, (viewModel.state.value as EventListState.Success).calendarMonth)
    }

    @Test
    fun `calendar can be opened and closed`() = runTest {
        coEvery { getEventsUseCase(any(), any(), any()) } returns flowOf(emptyList())
        viewModel.processIntent(EventListIntent.LoadEvents)

        viewModel.processIntent(EventListIntent.ToggleCalendar)
        assertTrue((viewModel.state.value as EventListState.Success).showCalendar)

        viewModel.processIntent(EventListIntent.ToggleCalendar)
        assertFalse((viewModel.state.value as EventListState.Success).showCalendar)
    }

    @Test
    fun `switching tab closes calendar`() = runTest {
        coEvery { getEventsUseCase(any(), any(), any()) } returns flowOf(emptyList())
        viewModel.processIntent(EventListIntent.LoadEvents)
        viewModel.processIntent(EventListIntent.ToggleCalendar)

        viewModel.processIntent(EventListIntent.SwitchTab(EventListTab.PAST))

        val state = viewModel.state.value as EventListState.Success
        assertEquals(EventListTab.PAST, state.selectedTab)
        assertFalse(state.showCalendar)
    }

    @Test
    fun `calendar rejects a date later than one year from today`() = runTest {
        coEvery { getEventsUseCase(any(), any(), any()) } returns flowOf(emptyList())
        viewModel.processIntent(EventListIntent.LoadEvents)
        val initialDate = (viewModel.state.value as EventListState.Success).selectedCalendarDate

        viewModel.processIntent(EventListIntent.SelectCalendarDate(LocalDate.now().plusYears(1).plusDays(1)))

        assertEquals(initialDate, (viewModel.state.value as EventListState.Success).selectedCalendarDate)
    }

    @Test
    fun `calendar accepts the final date of the one year window`() = runTest {
        coEvery { getEventsUseCase(any(), any(), any()) } returns flowOf(emptyList())
        viewModel.processIntent(EventListIntent.LoadEvents)
        val finalDate = LocalDate.now().plusYears(1)

        viewModel.processIntent(EventListIntent.SelectCalendarDate(finalDate))

        assertEquals(finalDate, (viewModel.state.value as EventListState.Success).selectedCalendarDate)
    }

    @Test
    fun `changing calendar month selects its first day`() = runTest {
        coEvery { getEventsUseCase(any(), any(), any()) } returns flowOf(emptyList())
        viewModel.processIntent(EventListIntent.LoadEvents)
        val expectedMonth = YearMonth.now().plusMonths(1)

        viewModel.processIntent(EventListIntent.ChangeCalendarMonth(1))

        val state = viewModel.state.value as EventListState.Success
        assertEquals(expectedMonth, state.calendarMonth)
        assertEquals(expectedMonth.atDay(1), state.selectedCalendarDate)
    }

    @Test
    fun `upcoming events are sorted by start date`() = runTest {
        val later = Event(1L, "Later", "", System.currentTimeMillis() + 20_000)
        val earlier = Event(2L, "Earlier", "", System.currentTimeMillis() + 10_000)
        coEvery { getEventsUseCase(any(), any(), any()) } returns flowOf(listOf(later, earlier))

        viewModel.processIntent(EventListIntent.LoadEvents)

        assertEquals(listOf(earlier, later), (viewModel.state.value as EventListState.Success).events)
    }

    @Test
    fun `past events are sorted from most recently finished`() = runTest {
        val older = Event(1L, "Older", "", System.currentTimeMillis() - 20_000)
        val newer = Event(2L, "Newer", "", System.currentTimeMillis() - 10_000)
        coEvery { getEventsUseCase(any(), any(), any()) } returns flowOf(listOf(older, newer))
        viewModel.processIntent(EventListIntent.LoadEvents)

        viewModel.processIntent(EventListIntent.SwitchTab(EventListTab.PAST))

        assertEquals(listOf(newer, older), (viewModel.state.value as EventListState.Success).events)
    }

    @Test
    fun `events returned for both loaded years are deduplicated`() = runTest {
        val normal = Event(1L, "Event", "", System.currentTimeMillis() + 10_000)
        val holiday = Event(2L, "Holiday", "", System.currentTimeMillis() + 20_000, EventType.Holiday)
        coEvery { getEventsUseCase(any(), any(), any()) } returns flowOf(listOf(normal, holiday))

        viewModel.processIntent(EventListIntent.LoadEvents)

        assertEquals(listOf(normal, holiday), (viewModel.state.value as EventListState.Success).allEvents)
    }

    @Test
    fun `different normal events on the same date are retained`() = runTest {
        val timestamp = System.currentTimeMillis() + 10_000
        val first = Event(1L, "First", "", timestamp)
        val second = Event(2L, "Second", "", timestamp)
        coEvery { getEventsUseCase(any(), any(), any()) } returns flowOf(listOf(first, second))

        viewModel.processIntent(EventListIntent.LoadEvents)

        assertEquals(listOf(first, second), (viewModel.state.value as EventListState.Success).events)
    }

    @Test
    fun `toggling holidays twice restores holiday events`() = runTest {
        val holiday = Event(2L, "Holiday", "", System.currentTimeMillis() + 20_000, EventType.Holiday)
        coEvery { getEventsUseCase(any(), any(), any()) } returns flowOf(listOf(holiday))
        viewModel.processIntent(EventListIntent.LoadEvents)

        viewModel.processIntent(EventListIntent.ToggleHolidays)
        viewModel.processIntent(EventListIntent.ToggleHolidays)

        val state = viewModel.state.value as EventListState.Success
        assertTrue(state.includeHolidays)
        assertEquals(listOf(holiday), state.events)
    }

    @Test
    fun `deleting unknown event does not call use case`() = runTest {
        coEvery { getEventsUseCase(any(), any(), any()) } returns flowOf(emptyList())
        viewModel.processIntent(EventListIntent.LoadEvents)

        viewModel.processIntent(EventListIntent.DeleteEvent(999L))

        coVerify(exactly = 0) { deleteEventUseCase(any()) }
    }

    @Test
    fun `reload preserves selected filters tab and calendar state`() = runTest {
        coEvery { getEventsUseCase(any(), any(), any()) } returns flowOf(emptyList())
        viewModel.processIntent(EventListIntent.LoadEvents)
        viewModel.processIntent(EventListIntent.SwitchTab(EventListTab.PAST))
        viewModel.processIntent(EventListIntent.ToggleHolidays)
        viewModel.processIntent(EventListIntent.ToggleCalendar)

        viewModel.processIntent(EventListIntent.LoadEvents)

        val state = viewModel.state.value as EventListState.Success
        assertEquals(EventListTab.PAST, state.selectedTab)
        assertFalse(state.includeHolidays)
        assertTrue(state.showCalendar)
    }
}
