package com.petra.manager

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsManager(private val context: Context) {

    val notificationsEnabled: Flow<Boolean> = context.dataStore.data
        .map {
            it[NOTIFICATIONS_ENABLED] ?: true
        }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit {
            it[NOTIFICATIONS_ENABLED] = enabled
        }
    }

    companion object {
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
    }
}