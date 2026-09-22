package com.icyapps.howmuchlonger.ui.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.icyapps.howmuchlonger.ui.screen.addevent.AddEventScreen
import com.icyapps.howmuchlonger.ui.screen.addevent.AddEventViewModel
import com.icyapps.howmuchlonger.ui.screen.eventlist.EventListScreen
import com.icyapps.howmuchlonger.ui.screen.eventlist.EventListViewModel

@Composable
fun AppNavigator() {
    val backStack = rememberNavBackStack(Routes.EventsList)

    // Use DisposableEffect to handle cleanup when the composable is disposed
    DisposableEffect(Unit) {
        onDispose {
            // Cleanup any pending animations or callbacks
            Log.d("AppNavigator", "Disposing AppNavigator")
        }
    }

    NavDisplay(
        backStack = backStack,
        onBack = {
            Log.d("AppNavigator", "Back navigation triggered")
            val removed = backStack.removeLastOrNull()
            if (removed != null) {
                true // Return true to indicate we've handled the back navigation
            } else {
                false // Return false if we couldn't handle the back navigation
            }
        },
        entryProvider = { key ->
            when (key) {
                is Routes.EventsList -> NavEntry(key) {
                    val viewModel: EventListViewModel = hiltViewModel()
                    val state by viewModel.state.collectAsState()

                    EventListScreen(
                        onNavigateToAddEvent = { initialDate ->
                            Log.d("AppNavigator", "onNavigateToAddEvent")
                            backStack.add(Routes.AddEditEvent(initialDate = initialDate))
                        },
                        onNavigateToEditEvent = { eventId ->
                            Log.d("AppNavigator", "onNavigateToEditEvent: $eventId")
                            backStack.add(Routes.AddEditEvent(eventId))
                        },
                        state = state,
                        onProcessIntent = { viewModel.processIntent(it) }
                    )
                }

                is Routes.AddEditEvent -> NavEntry(key) {
                    val viewModel: AddEventViewModel = hiltViewModel()
                    val state by viewModel.state.collectAsState()

                    // Always initialize the ViewModel with the eventId (null for create, id for edit)
                    LaunchedEffect(key.eventId, key.initialDate) {
                        viewModel.initialize(key.eventId, key.initialDate)
                    }

                    // Define the navigation function
                    val navigateBack: () -> Unit = {
                        Log.d("AppNavigator", "AddEventScreen: onNavigateBack called")
                        backStack.removeLastOrNull()
                    }

                    AddEventScreen(
                        onNavigateBack = navigateBack,
                        state = state,
                        onProcessIntent = { viewModel.processIntent(it) }
                    )
                }

                else -> error("Unknown navigation key: $key")
            }
        }
    )
}
