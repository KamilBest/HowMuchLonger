package com.icyapps.howmuchlonger.ui.screen.eventlist

import com.icyapps.howmuchlonger.domain.model.Event
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class CalendarRangeConnectionsTest {

    private val zone = ZoneId.systemDefault()
    private val start = LocalDate.of(2026, 9, 16)
    private val end = LocalDate.of(2026, 9, 18)
    private val range = Event(
        id = 1L,
        name = "Urlop",
        description = "",
        date = start.atStartOfDay(zone).toInstant().toEpochMilli(),
        endDate = end.atStartOfDay(zone).toInstant().toEpochMilli()
    )

    @Test
    fun `start of range connects only to next day`() {
        assertEquals(
            CalendarRangeConnections(previous = false, next = true),
            connections(start)
        )
    }

    @Test
    fun `middle of range connects both sides`() {
        assertEquals(
            CalendarRangeConnections(previous = true, next = true),
            connections(start.plusDays(1))
        )
    }

    @Test
    fun `end of range connects only to previous day`() {
        assertEquals(
            CalendarRangeConnections(previous = true, next = false),
            connections(end)
        )
    }

    @Test
    fun `range is capped at calendar week boundary`() {
        assertEquals(
            CalendarRangeConnections(previous = false, next = true),
            connections(start.plusDays(1), hasPreviousCell = false)
        )
        assertEquals(
            CalendarRangeConnections(previous = true, next = false),
            connections(start.plusDays(1), hasNextCell = false)
        )
    }

    @Test
    fun `day without range has no connections`() {
        assertEquals(
            CalendarRangeConnections(previous = false, next = false),
            calendarRangeConnections(
                date = start,
                rangeEvents = emptyList(),
                hasPreviousCell = true,
                hasNextCell = true
            )
        )
    }

    private fun connections(
        date: LocalDate,
        hasPreviousCell: Boolean = true,
        hasNextCell: Boolean = true
    ) = calendarRangeConnections(
        date = date,
        rangeEvents = listOf(range),
        hasPreviousCell = hasPreviousCell,
        hasNextCell = hasNextCell
    )
}
