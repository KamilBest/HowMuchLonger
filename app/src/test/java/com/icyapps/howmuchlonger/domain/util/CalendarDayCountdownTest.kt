package com.icyapps.howmuchlonger.domain.util

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class CalendarDayCountdownTest {
    private val warsaw = ZoneId.of("Europe/Warsaw")

    @Test
    fun `holiday countdown uses calendar days across daylight saving change`() {
        val now = LocalDate.of(2026, 9, 22).atTime(14, 36)
            .atZone(warsaw).toInstant().toEpochMilli()
        val christmas = LocalDate.of(2026, 12, 25).atStartOfDay(warsaw)
            .toInstant().toEpochMilli()

        assertEquals(94L, CalendarDayCountdown.daysUntil(christmas, now, warsaw))
    }

    @Test
    fun `holiday is today for the entire local date`() {
        val holiday = LocalDate.of(2026, 12, 25).atStartOfDay(warsaw)
            .toInstant().toEpochMilli()
        val noon = LocalDate.of(2026, 12, 25).atTime(12, 0)
            .atZone(warsaw).toInstant().toEpochMilli()

        assertEquals(0L, CalendarDayCountdown.daysUntil(holiday, noon, warsaw))
    }

    @Test
    fun `past holiday returns negative calendar days`() {
        val holiday = LocalDate.of(2026, 12, 25).atStartOfDay(warsaw)
            .toInstant().toEpochMilli()
        val nextDay = LocalDate.of(2026, 12, 26).atTime(8, 0)
            .atZone(warsaw).toInstant().toEpochMilli()

        assertEquals(-1L, CalendarDayCountdown.daysUntil(holiday, nextDay, warsaw))
    }
}
