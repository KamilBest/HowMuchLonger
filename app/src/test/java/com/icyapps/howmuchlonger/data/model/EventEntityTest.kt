package com.icyapps.howmuchlonger.data.model

import com.icyapps.howmuchlonger.domain.model.Event
import com.icyapps.howmuchlonger.domain.model.EventType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class EventEntityTest {

    @Test
    fun `entity mapping preserves a normal event date range`() {
        val entity = EventEntity(
            id = 42L,
            name = "Urlop",
            description = "Wyjazd",
            date = 1_800_000_000_000L,
            type = EventType.Normal,
            endDate = 1_800_259_200_000L
        )

        val event = entity.toDomainModel()

        assertEquals(entity.id, event.id)
        assertEquals(entity.name, event.name)
        assertEquals(entity.description, event.description)
        assertEquals(entity.date, event.date)
        assertEquals(entity.type, event.type)
        assertEquals(entity.endDate, event.endDate)
    }

    @Test
    fun `domain mapping preserves a single date event`() {
        val event = Event(
            id = 7L,
            name = "Koncert",
            description = "",
            date = 1_900_000_000_000L,
            type = EventType.Normal
        )

        val entity = event.toEntity()

        assertEquals(event.id, entity.id)
        assertEquals(event.name, entity.name)
        assertEquals(event.description, entity.description)
        assertEquals(event.date, entity.date)
        assertEquals(event.type, entity.type)
        assertNull(entity.endDate)
        assertNull(entity.countryCode)
    }

    @Test
    fun `holiday entity maps to a non editable holiday domain event`() {
        val entity = EventEntity(
            id = 3L,
            name = "Święto",
            description = "Holiday",
            date = 1_850_000_000_000L,
            type = EventType.Holiday,
            countryCode = "PL"
        )

        val event = entity.toDomainModel()

        assertEquals(EventType.Holiday, event.type)
        assertNull(event.endDate)
    }
}
