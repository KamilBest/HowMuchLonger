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
import androidx.compose.ui.text.font.FontWeight
import com.icyapps.howmuchlonger.domain.util.DurationFormatter
import com.icyapps.howmuchlonger.ui.theme.Accent
import kotlinx.coroutines.delay
import java.util.Locale
import java.util.concurrent.TimeUnit

@Composable
fun CountdownText(
    targetTimeInMs: Long,
    isPastTab: Boolean = false,
    locale: Locale = Locale.getDefault(),
    color: Color = Accent,
    style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.bodyMedium
) {
    var currentTimeInMs by remember { mutableStateOf(System.currentTimeMillis()) }

    val displayText by remember {
        derivedStateOf {
            if (isPastTab) {
                "Past event"
            } else {
                val remainingMillis = targetTimeInMs - currentTimeInMs
                if (remainingMillis <= 0) {
                    "Past event"
                } else {
                    val showSeconds = remainingMillis <= TimeUnit.MINUTES.toMillis(1)
                    DurationFormatter.format(remainingMillis, locale, showSeconds)
                }
            }
        }
    }

    val updateDelay = remember {
        derivedStateOf {
            if (isPastTab) {
                Long.MAX_VALUE // Don't update past events
            } else {
                val remainingMillis = targetTimeInMs - currentTimeInMs
                when {
                    remainingMillis <= 0 -> Long.MAX_VALUE // Don't update past events
                    remainingMillis <= TimeUnit.MINUTES.toMillis(1) -> 1_000L
                    remainingMillis > TimeUnit.HOURS.toMillis(1) -> 60_000L
                    else -> 10_000L
                }
            }
        }
    }

    LaunchedEffect(targetTimeInMs, isPastTab) {
        // Only update for future events in upcoming tab
        if (!isPastTab) {
            while (true) {
                currentTimeInMs = System.currentTimeMillis()
                // Check if event has passed
                if (currentTimeInMs >= targetTimeInMs) {
                    break // Stop updating when event becomes past
                }
                delay(updateDelay.value)
            }
        }
    }

    Text(
        text = displayText,
        style = style,
        color = color,
        fontWeight = FontWeight.Bold
    )
}
