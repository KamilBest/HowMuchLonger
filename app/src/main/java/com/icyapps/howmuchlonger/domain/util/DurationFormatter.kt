package com.icyapps.howmuchlonger.domain.util

import android.icu.text.MeasureFormat
import android.icu.util.Measure
import android.icu.util.MeasureUnit
import java.util.Locale
import java.util.concurrent.TimeUnit

object DurationFormatter {

    fun formatDays(days: Long, locale: Locale = Locale.getDefault()): String {
        val formatter = MeasureFormat.getInstance(locale, MeasureFormat.FormatWidth.SHORT)
        return formatter.format(Measure(days.coerceAtLeast(0), MeasureUnit.DAY))
    }

    internal enum class Unit {
        DAY,
        HOUR,
        MINUTE,
        SECOND
    }

    internal data class Part(val value: Long, val unit: Unit)

    fun format(
        durationInMillis: Long,
        locale: Locale = Locale.getDefault(),
        showSeconds: Boolean = true
    ): String {
        if (durationInMillis <= 0) return "0s"

        val parts = breakdown(durationInMillis, showSeconds)
        val days = parts.firstOrNull { it.unit == Unit.DAY }?.value ?: 0
        val hours = parts.firstOrNull { it.unit == Unit.HOUR }?.value ?: 0
        val minutes = parts.firstOrNull { it.unit == Unit.MINUTE }?.value ?: 0
        val seconds = parts.firstOrNull { it.unit == Unit.SECOND }?.value ?: 0

        // For durations under 1 hour, show both minutes and seconds
        if (showSeconds && days == 0L && hours == 0L) {
            return when {
                minutes > 0 -> "${minutes}m ${seconds}s"
                else -> "${seconds}s"
            }
        }

        val measures = parts.map { part ->
            val measureUnit = when (part.unit) {
                Unit.DAY -> MeasureUnit.DAY
                Unit.HOUR -> MeasureUnit.HOUR
                Unit.MINUTE -> MeasureUnit.MINUTE
                Unit.SECOND -> MeasureUnit.SECOND
            }
            Measure(part.value, measureUnit)
        }

        val formatter = MeasureFormat.getInstance(locale, MeasureFormat.FormatWidth.SHORT)
        return formatter.formatMeasures(*measures.toTypedArray())
    }

    internal fun breakdown(durationInMillis: Long, showSeconds: Boolean): List<Part> {
        val days = TimeUnit.MILLISECONDS.toDays(durationInMillis)
        val hours = TimeUnit.MILLISECONDS.toHours(durationInMillis) % 24
        val minutes = TimeUnit.MILLISECONDS.toMinutes(durationInMillis) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(durationInMillis) % 60

        return buildList {
            if (days > 0) add(Part(days, Unit.DAY))
            if (hours > 0) add(Part(hours, Unit.HOUR))
            if (minutes > 0) add(Part(minutes, Unit.MINUTE))
            if (showSeconds && (seconds > 0 || isEmpty())) {
                add(Part(seconds, Unit.SECOND))
            }
        }
    }
}
