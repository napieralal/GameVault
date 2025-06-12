package com.example.gamevault

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.gamevault.ui.screens.games.GameListScreen.GameListScreen
import com.example.gamevault.ui.theme.GameVaultTheme
import com.google.firebase.FirebaseApp


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        FirebaseApp.initializeApp(this)

        val initialTheme = ThemeHelper.loadThemePreference(this)

        setContent {
            var isDarkTheme by remember { mutableStateOf(initialTheme) }

            GameVaultTheme(darkTheme = isDarkTheme) {
                GameVaultApp(
                    isDarkTheme = isDarkTheme,
                    onToggleTheme = {
                        isDarkTheme = !isDarkTheme
                        ThemeHelper.saveThemePreference(this, isDarkTheme)
                    }
                )
            }
        }
    }
}

