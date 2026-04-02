package com.example.pertracker.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsDataStore(private val context: Context) {
    companion object {
        val IS_AUTO_SYNC_ENABLED = booleanPreferencesKey("is_auto_sync_enabled")
        val SYNC_URL = stringPreferencesKey("sync_url")
        val API_KEY = stringPreferencesKey("api_key")
    }

    val isAutoSyncEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[IS_AUTO_SYNC_ENABLED] ?: false
        }

    suspend fun setAutoSyncEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_AUTO_SYNC_ENABLED] = enabled
        }
    }

    val syncUrl: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[SYNC_URL] ?: ""
        }

    val apiKey: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[API_KEY] ?: ""
        }

    suspend fun setSyncUrl(url: String) {
        context.dataStore.edit { preferences ->
            preferences[SYNC_URL] = url
        }
    }

    suspend fun setApiKey(key: String) {
        context.dataStore.edit { preferences ->
            preferences[API_KEY] = key
        }
    }
}
