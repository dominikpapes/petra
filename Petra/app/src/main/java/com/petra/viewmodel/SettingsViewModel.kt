package com.petra.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.petra.manager.SettingsManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(private val settingsManager: SettingsManager) : ViewModel() {

    val notificationsEnabled: StateFlow<Boolean> = settingsManager.notificationsEnabled
        .stateIn(viewModelScope, SharingStarted.Lazily, true)

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsManager.setNotificationsEnabled(enabled)
        }
    }
}