package com.icyapps.howmuchlonger

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.icyapps.howmuchlonger.ui.navigation.AppNavigator
import com.icyapps.howmuchlonger.ui.theme.HowMuchLongerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HowMuchLongerTheme {
                AppNavigator()
            }
        }
    }
}