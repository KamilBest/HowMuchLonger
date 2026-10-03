package com.icyapps.howmuchlonger.ui.screen.eventlist

import com.icyapps.howmuchlonger.domain.model.Event
import com.icyapps.howmuchlonger.domain.model.EventType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class EventKeyTest {
    @Test
    fun `holidays without ids still get unique keys`() {
        val events = listOf(
            Event(0L, "Nowy Rok", "", 1L, EventType.Holiday, countryCode = "PL"),
            Event(0L, "Święto Pracy", "", 2L, EventType.Holiday, countryCode = "PL"),
            Event(0L, "Neujahr", "", 1L, EventType.Holiday, countryCode = "DE")
        )

        assertEquals(events.size, events.map(::eventKey).distinct().size)
    }

    @Test
    fun `normal event and holiday with the same id get different keys`() {
        val normal = Event(5L, "Trip", "", 1L)
        val holiday = Event(5L, "Holiday", "", 1L, EventType.Holiday, countryCode = "PL")

        assertNotEquals(eventKey(normal), eventKey(holiday))
    }
}
