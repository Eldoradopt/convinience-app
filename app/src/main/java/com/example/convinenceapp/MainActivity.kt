package com.example.convinenceapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.convinenceapp.ui.navigation.AppNavigation
import com.example.convinenceapp.ui.theme.ConvinenceappTheme
import com.example.convinenceapp.ui.theme.ThemeState

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeMode by ThemeState.theme.collectAsState()
            ConvinenceappTheme(themeMode = themeMode) {
                AppNavigation()
            }
        }
    }
}
