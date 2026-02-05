package com.metaclean.app.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.metaclean.app.MetaCleanApplication
import com.metaclean.app.ui.navigation.MetaCleanNavigation
import com.metaclean.app.ui.screen.lock.AppLockScreen
import com.metaclean.app.ui.theme.MetaCleanTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val app = application as MetaCleanApplication
        val preferencesRepository = app.preferencesRepository
        
        setContent {
            val isDarkMode by preferencesRepository.isDarkMode.collectAsStateWithLifecycle(
                initialValue = isSystemInDarkTheme()
            )
            val isAppLockEnabled by preferencesRepository.isAppLockEnabled.collectAsStateWithLifecycle(
                initialValue = false
            )
            val colorTheme by preferencesRepository.appColorTheme.collectAsStateWithLifecycle(
                initialValue = "yellow"
            )
            
            var isAuthenticated by remember { mutableStateOf(!isAppLockEnabled) }
            
            MetaCleanTheme(darkTheme = isDarkMode, colorTheme = colorTheme) {
                if (isAppLockEnabled && !isAuthenticated) {
                    AppLockScreen(
                        onAuthenticated = { isAuthenticated = true },
                        onFailure = { finish() }
                    )
                } else {
                    MetaCleanNavigation()
                }
            }
        }
    }
}
