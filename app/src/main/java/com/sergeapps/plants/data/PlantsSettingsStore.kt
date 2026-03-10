package com.sergeapps.plants.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "plants_settings")

data class PlantsSettings(
    val baseUrl: String,
    val port: String,
    val deviceId: String,
    val apiKey: String
) {
    companion object {
        val Default = PlantsSettings(
            baseUrl = "https://homeapi.ddns.net",
            port = "443",
            deviceId = "",
            apiKey = ""
        )
    }
}

class PlantsSettingsStore(private val context: Context) {

    private object Keys {
        val BaseUrl = stringPreferencesKey("base_url")
        val Port = stringPreferencesKey("port")
        val DeviceId = stringPreferencesKey("device_id")
        val ApiKey = stringPreferencesKey("api_key")
    }

    val settingsFlow: Flow<PlantsSettings> =
        context.dataStore.data.map { prefs ->
            PlantsSettings(
                baseUrl = prefs[Keys.BaseUrl] ?: PlantsSettings.Default.baseUrl,
                port = prefs[Keys.Port] ?: PlantsSettings.Default.port,
                deviceId = prefs[Keys.DeviceId] ?: PlantsSettings.Default.deviceId,
                apiKey = prefs[Keys.ApiKey] ?: PlantsSettings.Default.apiKey
            )
        }

    suspend fun save(newSettings: PlantsSettings) {
        context.dataStore.edit { prefs ->
            prefs[Keys.BaseUrl] = newSettings.baseUrl
            prefs[Keys.Port] = newSettings.port
            prefs[Keys.DeviceId] = newSettings.deviceId
            prefs[Keys.ApiKey] = newSettings.apiKey
        }
    }
}
