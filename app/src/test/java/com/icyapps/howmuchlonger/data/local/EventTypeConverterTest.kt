package com.icyapps.howmuchlonger.data.local

import com.icyapps.howmuchlonger.domain.model.EventType
import org.junit.Assert.assertEquals
import org.junit.Test

class EventTypeConverterTest {

    private val converter = EventTypeConverter()

    @Test
    fun `all event types survive database conversion`() {
        EventType.entries.forEach { type ->
            assertEquals(type, converter.toEventType(converter.fromEventType(type)))
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun `unknown database value is rejected`() {
        converter.toEventType("Unknown")
    }
}
