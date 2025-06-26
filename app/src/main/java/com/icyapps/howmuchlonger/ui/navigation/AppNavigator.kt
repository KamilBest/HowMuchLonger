package com.icyapps.howmuchlonger.ui.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import com.icyapps.howmuchlonger.ui.screen.addevent.AddEventScreen
import com.icyapps.howmuchlonger.ui.screen.eventlist.EventListScreen

@Composable
fun AppNavigator() {
    val backStack = remember { mutableStateListOf<Routes>(Routes.EventsList) }
    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = { key ->
            when (key) {
                is Routes.EventsList -> NavEntry(key) {
                    EventListScreen(
                        onNavigateToAddEvent = {
                            Log.d("AppNavigator", "onNavigateToAddEvent")
                            backStack.add(Routes.AddEditEvent())
                        },
                        onNavigateToEditEvent = { eventId ->
                            Log.d("AppNavigator", "onNavigateToEditEvent: $eventId")
                            backStack.add(Routes.AddEditEvent(eventId))
                        }
                    )
                }

                is Routes.AddEditEvent -> NavEntry(key) {
                    AddEventScreen(
                        eventId = key.eventId,
                        onNavigateBack = {
                            backStack.removeLastOrNull()
                        }
                    )
                }
            }
        }
    )
}
