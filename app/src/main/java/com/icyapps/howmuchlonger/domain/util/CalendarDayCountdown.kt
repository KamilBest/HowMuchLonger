package com.icyapps.howmuchlonger.domain.util

import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit

object CalendarDayCountdown {
    fun daysUntil(
        targetTimeInMillis: Long,
        currentTimeInMillis: Long,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): Long {
        val currentDate = Instant.ofEpochMilli(currentTimeInMillis)
            .atZone(zoneId)
            .toLocalDate()
        val targetDate = Instant.ofEpochMilli(targetTimeInMillis)
            .atZone(zoneId)
            .toLocalDate()
        return ChronoUnit.DAYS.between(currentDate, targetDate)
    }
}
