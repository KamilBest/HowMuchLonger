package com.icyapps.howmuchlonger.domain.util

import android.icu.text.MeasureFormat
import android.icu.util.Measure
import android.icu.util.MeasureUnit
import java.util.Locale
import java.util.concurrent.TimeUnit

object DurationFormatter {

    fun format(
        durationInMillis: Long,
        locale: Locale = Locale.getDefault(),
        showSeconds: Boolean = true
    ): String {
        if (durationInMillis <= 0) return "0s"

        val days = TimeUnit.MILLISECONDS.toDays(durationInMillis)
        val hours = TimeUnit.MILLISECONDS.toHours(durationInMillis) % 24
        val minutes = TimeUnit.MILLISECONDS.toMinutes(durationInMillis) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(durationInMillis) % 60

        val measures = buildList {
            if (days > 0) add(Measure(days, MeasureUnit.DAY))
            if (hours > 0) add(Measure(hours, MeasureUnit.HOUR))
            if (minutes > 0) add(Measure(minutes, MeasureUnit.MINUTE))
            if (showSeconds && (seconds > 0 || isEmpty())) {
                add(Measure(seconds, MeasureUnit.SECOND))
            }
        }

        val formatter = MeasureFormat.getInstance(locale, MeasureFormat.FormatWidth.SHORT)
        return formatter.formatMeasures(*measures.toTypedArray())
    }
}