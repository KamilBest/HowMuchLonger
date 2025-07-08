package com.icyapps.howmuchlonger.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
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
    if (isPastTab) {
        Text(
            text = "Past event",
            style = style,
            color = color,
            fontWeight = FontWeight.Bold
        )
        return
    }

    val now = System.currentTimeMillis()
    val initialRemaining = targetTimeInMs - now
    val showCountdown = initialRemaining > 0

    var remainingMillis by remember(targetTimeInMs) { mutableLongStateOf(initialRemaining) }
    val currentTargetTime by rememberUpdatedState(targetTimeInMs)

    if (showCountdown) {
        LaunchedEffect(currentTargetTime) {
            while (remainingMillis > 0) {
                val delayMillis = when {
                    remainingMillis <= TimeUnit.MINUTES.toMillis(1) -> 1_000L
                    remainingMillis > TimeUnit.HOURS.toMillis(1) -> 60_000L
                    else -> 10_000L
                }
                delay(delayMillis)
                remainingMillis = currentTargetTime - System.currentTimeMillis()
            }
        }
    }

    val displayText = if (!showCountdown || remainingMillis <= 0) {
        "Past event"
    } else {
        val showSeconds = remainingMillis <= TimeUnit.MINUTES.toMillis(1)
        DurationFormatter.format(remainingMillis, locale, showSeconds)
    }

    Text(
        text = displayText,
        style = style,
        color = color,
        fontWeight = FontWeight.Bold
    )
}
