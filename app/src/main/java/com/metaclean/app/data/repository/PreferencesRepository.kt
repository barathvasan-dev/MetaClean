package com.metaclean.app.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class PreferencesRepository(private val context: Context) {
    
    private object PreferencesKeys {
        val IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")
        val IS_APP_LOCK_ENABLED = booleanPreferencesKey("is_app_lock_enabled")
        val DEFAULT_PRESET = stringPreferencesKey("default_preset")
        val SAVE_AS_COPY = booleanPreferencesKey("save_as_copy")
        val SHOW_WARNING = booleanPreferencesKey("show_warning")
        val APP_COLOR_THEME = stringPreferencesKey("app_color_theme")
    }
    
    val isDarkMode: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.IS_DARK_MODE] ?: false
    }
    
    val isAppLockEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.IS_APP_LOCK_ENABLED] ?: false
    }
    
    val defaultPreset: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.DEFAULT_PRESET] ?: "ANONYMOUS"
    }
    
    val saveAsCopy: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.SAVE_AS_COPY] ?: true
    }
    
    val appColorTheme: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.APP_COLOR_THEME] ?: "yellow"
    }
    
    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_DARK_MODE] = enabled
        }
    }
    
    suspend fun setAppLock(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_APP_LOCK_ENABLED] = enabled
        }
    }
    
    suspend fun setAppColorTheme(theme: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.APP_COLOR_THEME] = theme
        }
    }
    
    suspend fun setDefaultPreset(preset: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DEFAULT_PRESET] = preset
        }
    }
    
    suspend fun setSaveAsCopy(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SAVE_AS_COPY] = enabled
        }
    }
}
