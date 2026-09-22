package com.icyapps.howmuchlonger.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalLocale
import com.icyapps.howmuchlonger.R
import com.icyapps.howmuchlonger.domain.util.CalendarDayCountdown
import com.icyapps.howmuchlonger.domain.util.DurationFormatter
import com.icyapps.howmuchlonger.ui.theme.Accent
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.ZoneId
import java.util.concurrent.TimeUnit

@Composable
fun CountdownText(
    targetTimeInMs: Long,
    isPastTab: Boolean = false,
    endsIn: Boolean = false,
    calendarDaysOnly: Boolean = false,
    color: Color = Accent,
    style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.bodyMedium
) {
    val locale = LocalLocale.current.platformLocale
    var nowInMillis by remember(targetTimeInMs) { mutableLongStateOf(System.currentTimeMillis()) }
    val initialRemaining = targetTimeInMs - nowInMillis

    var remainingMillis by remember(targetTimeInMs) { mutableLongStateOf(initialRemaining) }
    val currentTargetTime by rememberUpdatedState(targetTimeInMs)

    LaunchedEffect(currentTargetTime, calendarDaysOnly) {
        while (true) {
            nowInMillis = System.currentTimeMillis()
            remainingMillis = currentTargetTime - nowInMillis
            if (calendarDaysOnly) {
                val zone = ZoneId.systemDefault()
                val nextMidnight = Instant.ofEpochMilli(nowInMillis)
                    .atZone(zone)
                    .toLocalDate()
                    .plusDays(1)
                    .atStartOfDay(zone)
                    .toInstant()
                    .toEpochMilli()
                delay((nextMidnight - nowInMillis).coerceAtLeast(1_000L))
                continue
            }
            val absoluteDuration = kotlin.math.abs(remainingMillis)
            val delayMillis = when {
                absoluteDuration <= TimeUnit.MINUTES.toMillis(1) -> 1_000L
                absoluteDuration > TimeUnit.HOURS.toMillis(1) -> 60_000L
                else -> 10_000L
            }
            delay(delayMillis)
        }
    }

    if (calendarDaysOnly) {
        val days = CalendarDayCountdown.daysUntil(
            targetTimeInMillis = targetTimeInMs,
            currentTimeInMillis = nowInMillis
        )
        val displayText = when {
            days == 0L -> stringResource(R.string.today)
            isPastTab || days < 0 -> stringResource(
                R.string.time_ago,
                DurationFormatter.formatDays(kotlin.math.abs(days), locale)
            )
            else -> DurationFormatter.formatDays(days, locale)
        }
        Text(
            text = displayText,
            style = style,
            color = color,
            fontWeight = FontWeight.Bold
        )
        return
    }

    val isPast = isPastTab || remainingMillis <= 0
    val durationText = DurationFormatter.format(
        durationInMillis = kotlin.math.abs(remainingMillis),
        locale = locale,
        showSeconds = kotlin.math.abs(remainingMillis) <= TimeUnit.MINUTES.toMillis(1)
    )
    val displayText = when {
        isPast -> stringResource(R.string.time_ago, durationText)
        endsIn -> stringResource(R.string.ends_in, durationText)
        else -> durationText
    }

    Text(
        text = displayText,
        style = style,
        color = color,
        fontWeight = FontWeight.Bold
    )
}
