package com.icyapps.howmuchlonger.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import com.icyapps.howmuchlonger.domain.util.DurationFormatter
import kotlinx.coroutines.delay
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Locale
import java.util.concurrent.TimeUnit

@Composable
fun CountdownText(
    targetTimeInMs: Long,
    locale: Locale = Locale.getDefault(),
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    var now by remember { mutableStateOf(LocalDateTime.now()) }

    val targetDateTime = remember(targetTimeInMs) {
        Instant.ofEpochMilli(targetTimeInMs)
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime()
    }

    val remainingMillis by remember {
        derivedStateOf {
            Duration.between(now, targetDateTime)
                .coerceAtLeast(Duration.ZERO)
                .toMillis()
        }
    }

    val showSeconds by remember {
        derivedStateOf { remainingMillis <= TimeUnit.MINUTES.toMillis(1) }
    }

    val displayText by remember {
        derivedStateOf {
            DurationFormatter.format(remainingMillis, locale, showSeconds)
        }
    }

    val updateDelay = when {
        showSeconds -> 1_000L
        remainingMillis > TimeUnit.HOURS.toMillis(1) -> 60_000L
        else -> 10_000L
    }

    LaunchedEffect(targetDateTime, updateDelay) {
        while (remainingMillis > 0) {
            now = LocalDateTime.now()
            delay(updateDelay)
        }
    }

    Text(
        text = displayText,
        style = MaterialTheme.typography.bodyMedium,
        color = color
    )
}
