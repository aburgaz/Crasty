package com.crasty.app

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.crasty.app.data.settings.ThemeMode
import com.crasty.app.ui.navigation.CrastyNavHost
import com.crasty.app.ui.theme.CrastyTheme
import com.crasty.app.ui.theme.LocalProjectColors

class MainActivity : ComponentActivity() {

    private val requestNotifications =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestNotifications.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        val container = (application as CrastyApp).container
        val settings = container.settingsRepository
        val projectColorsFlow = container.taskRepository.observeProjectColors()

        setContent {
            val themeMode by settings.themeMode.collectAsStateWithLifecycle()
            val projectColors by projectColorsFlow.collectAsStateWithLifecycle(emptyMap())
            val darkTheme = when (themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }
            // Iconos de la barra de estado acordes al tema elegido (no solo al del sistema).
            DisposableEffect(darkTheme) {
                val style = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { darkTheme }
                enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
                onDispose { }
            }
            CrastyTheme(darkTheme = darkTheme) {
                CompositionLocalProvider(LocalProjectColors provides projectColors) {
                    CrastyNavHost()
                }
            }
        }
    }
}
