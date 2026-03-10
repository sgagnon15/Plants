package com.sergeapps.plants.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sergeapps.plants.data.PlantsSettings
import com.sergeapps.plants.data.PlantsSettingsStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class SettingsUiState(
    val baseUrl: String = "",
    val port: String = "",
    val deviceId: String = "",
    val apiKey: String = "",
    val savedMessage: String? = null
)

class SettingsViewModel(app: Application) : AndroidViewModel(app) {

    private val store = PlantsSettingsStore(app)

    private val uiState = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = uiState.asStateFlow()

    fun load() {
        viewModelScope.launch {
            val s: PlantsSettings = store.settingsFlow.first()
            uiState.value = uiState.value.copy(
                baseUrl = s.baseUrl,
                port = s.port,
                deviceId = s.deviceId,
                apiKey = s.apiKey
            )
        }
    }

    fun setBaseUrl(v: String) { uiState.value = uiState.value.copy(baseUrl = v, savedMessage = null) }
    fun setPort(v: String) { uiState.value = uiState.value.copy(port = v, savedMessage = null) }
    fun setDeviceId(v: String) { uiState.value = uiState.value.copy(deviceId = v, savedMessage = null) }
    fun setApiKey(v: String) { uiState.value = uiState.value.copy(apiKey = v, savedMessage = null) }

    fun save() {
        viewModelScope.launch {
            store.save(
                PlantsSettings(
                    baseUrl = uiState.value.baseUrl,
                    port = uiState.value.port,
                    deviceId = uiState.value.deviceId,
                    apiKey = uiState.value.apiKey
                )
            )
            uiState.value = uiState.value.copy(savedMessage = "Enregistré ✅")
        }
    }
}
