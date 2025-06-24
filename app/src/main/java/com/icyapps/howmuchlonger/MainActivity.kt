package com.icyapps.howmuchlonger

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import com.icyapps.howmuchlonger.ui.navigation.AppNavigator
import com.icyapps.howmuchlonger.ui.navigation.Routes
import com.icyapps.howmuchlonger.ui.screen.addevent.AddEventScreen
import com.icyapps.howmuchlonger.ui.screen.eventlist.EventListScreen
import com.icyapps.howmuchlonger.ui.theme.HowMuchLongerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HowMuchLongerTheme {
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
                                        backStack.add(Routes.AddEditEvent)
                                    }
                                )
                            }

                            is Routes.AddEditEvent -> NavEntry(key) {
                                AddEventScreen(
                                    onNavigateBack = {
                                        backStack.removeLastOrNull()
                                    }
                                )
                            }
                        }
                    }
                )
            }
        }
    }
}