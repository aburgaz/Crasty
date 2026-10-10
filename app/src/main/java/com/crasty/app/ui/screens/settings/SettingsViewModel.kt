package com.crasty.app.ui.screens.settings

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.crasty.app.data.backup.BackupManager
import com.crasty.app.data.settings.SettingsRepository
import com.crasty.app.data.settings.ThemeMode
import com.crasty.app.di.appContainer
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed interface BackupResult {
    data class Exported(val count: Int) : BackupResult
    data class Imported(val count: Int) : BackupResult
    data object Failed : BackupResult
}

class SettingsViewModel(
    private val backupManager: BackupManager,
    private val settings: SettingsRepository,
) : ViewModel() {

    val themeMode: StateFlow<ThemeMode> = settings.themeMode

    var result by mutableStateOf<BackupResult?>(null)
        private set

    fun setThemeMode(mode: ThemeMode) = settings.setThemeMode(mode)

    fun export(uri: Uri) {
        viewModelScope.launch {
            result = runCatching { BackupResult.Exported(backupManager.export(uri)) }
                .getOrDefault(BackupResult.Failed)
        }
    }

    fun import(uri: Uri) {
        viewModelScope.launch {
            result = runCatching { BackupResult.Imported(backupManager.import(uri)) }
                .getOrDefault(BackupResult.Failed)
        }
    }

    fun resultShown() {
        result = null
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val container = appContainer()
                SettingsViewModel(container.backupManager, container.settingsRepository)
            }
        }
    }
}
