package com.icyapps.howmuchlonger.domain.util

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.concurrent.TimeUnit

class DurationFormatterTest {

    @Test
    fun `non positive duration is formatted as zero seconds`() {
        assertEquals("0s", DurationFormatter.format(0))
        assertEquals("0s", DurationFormatter.format(-1))
    }

    @Test
    fun `duration shorter than a minute shows seconds`() {
        assertEquals("45s", DurationFormatter.format(TimeUnit.SECONDS.toMillis(45)))
    }

    @Test
    fun `duration shorter than an hour shows minutes and remaining seconds`() {
        val duration = TimeUnit.MINUTES.toMillis(12) + TimeUnit.SECONDS.toMillis(5)

        assertEquals("12m 5s", DurationFormatter.format(duration))
    }

    @Test
    fun `breakdown keeps every non zero duration component`() {
        val duration = TimeUnit.DAYS.toMillis(2) +
            TimeUnit.HOURS.toMillis(3) +
            TimeUnit.MINUTES.toMillis(4) +
            TimeUnit.SECONDS.toMillis(5)

        assertEquals(
            listOf(
                DurationFormatter.Part(2, DurationFormatter.Unit.DAY),
                DurationFormatter.Part(3, DurationFormatter.Unit.HOUR),
                DurationFormatter.Part(4, DurationFormatter.Unit.MINUTE),
                DurationFormatter.Part(5, DurationFormatter.Unit.SECOND)
            ),
            DurationFormatter.breakdown(duration, showSeconds = true)
        )
    }

    @Test
    fun `breakdown can omit seconds`() {
        val duration = TimeUnit.HOURS.toMillis(1) + TimeUnit.SECONDS.toMillis(30)

        assertEquals(
            listOf(DurationFormatter.Part(1, DurationFormatter.Unit.HOUR)),
            DurationFormatter.breakdown(duration, showSeconds = false)
        )
    }

    @Test
    fun `breakdown keeps zero seconds when no larger component exists`() {
        assertEquals(
            listOf(DurationFormatter.Part(0, DurationFormatter.Unit.SECOND)),
            DurationFormatter.breakdown(0, showSeconds = true)
        )
    }
}
