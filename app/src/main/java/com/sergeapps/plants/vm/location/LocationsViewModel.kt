package com.sergeapps.plants.vm.location

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sergeapps.plants.data.PlantsRepository
import com.sergeapps.plants.data.PlantsSettingsStore
import com.sergeapps.plants.data.api.LocationDto
import com.sergeapps.plants.data.api.PlantsApiFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

data class LocationsUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val locations: List<LocationDto> = emptyList(),
    val isAddDialogOpen: Boolean = false,
    val newLocationText: String = "",
    val newNbBinText: String = "1",
    val isEditDialogOpen: Boolean = false,
    val editId: Int? = null,
    val editLocationText: String = "",
    val editNbBinText: String = "",
    val editTypeText: String = "",
    val usageCountById: Map<Int, Int> = emptyMap(),
    val isUsageLoading: Boolean = false,
    val isDeleteConfirmOpen: Boolean = false,
    val deleteCandidateId: Int? = null,
    val deleteCandidateName: String = ""
)

class LocationsViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val settingsStore = PlantsSettingsStore(application)

    private val uiState = MutableStateFlow(LocationsUiState())
    val state: StateFlow<LocationsUiState> = uiState.asStateFlow()

    fun load() {
        viewModelScope.launch {
            uiState.update { it.copy(isLoading = true, error = null) }

            runCatching {
                val settings = settingsStore.settingsFlow.first()
                val api = PlantsApiFactory.create(settings)
                val repository = PlantsRepository(api)
                repository.getLocations()
            }.onSuccess { list ->
                uiState.update { it.copy(isLoading = false, locations = list) }
                refreshUsageCounts()
            }.onFailure { ex ->
                uiState.update { it.copy(isLoading = false, error = ex.message ?: "Erreur") }
            }
        }
    }
    fun openAddDialog() = uiState.update { it.copy(isAddDialogOpen = true) }
    fun closeAddDialog() = uiState.update { it.copy(isAddDialogOpen = false) }

    fun onNewLocationChanged(value: String) = uiState.update { it.copy(newLocationText = value) }
    fun onNewNbBinChanged(value: String) = uiState.update { it.copy(newNbBinText = value) }

    fun createLocation() {
        viewModelScope.launch {
            val location = state.value.newLocationText.trim()
            val nbBin = state.value.newNbBinText.trim().toIntOrNull() ?: 0

            if (location.isBlank() || nbBin <= 0) {
                uiState.update { it.copy(error = "Location et nbbin obligatoires") }
                return@launch
            }

            uiState.update { it.copy(isLoading = true, error = null) }

            runCatching {
                val settings = settingsStore.settingsFlow.first()
                val api = PlantsApiFactory.create(settings)
                val repository = PlantsRepository(api)
                repository.createLocation(location = location, nbBin = nbBin)
            }.onSuccess {
                uiState.update {
                    it.copy(
                        isLoading = false,
                        isAddDialogOpen = false,
                        newLocationText = "",
                        newNbBinText = "1"
                    )
                }
                load()
            }.onFailure { ex ->
                uiState.update { it.copy(isLoading = false, error = ex.message ?: "Erreur") }
            }
        }
    }

    fun openEditDialog(dto: com.sergeapps.plants.data.api.LocationDto) {
        uiState.update {
            it.copy(
                isEditDialogOpen = true,
                editId = dto.id,
                editLocationText = dto.location,
                editNbBinText = dto.nbBin.toString(),
                editTypeText = dto.type.orEmpty()
            )
        }
    }

    fun closeEditDialog() {
        uiState.update { it.copy(isEditDialogOpen = false, editId = null) }
    }

    fun onEditLocationChanged(value: String) = uiState.update { it.copy(editLocationText = value) }
    fun onEditNbBinChanged(value: String) = uiState.update { it.copy(editNbBinText = value) }
    fun onEditTypeChanged(value: String) = uiState.update { it.copy(editTypeText = value) }

    fun saveEdit() {
        viewModelScope.launch {
            val id = state.value.editId ?: return@launch
            val location = state.value.editLocationText.trim()
            val nbBin = state.value.editNbBinText.trim().toIntOrNull() ?: 0
            val type = state.value.editTypeText.trim()

            if (location.isBlank() || nbBin <= 0) {
                uiState.update { it.copy(error = "Location et nbbin obligatoires") }
                return@launch
            }

            uiState.update { it.copy(isLoading = true, error = null) }

            runCatching {
                val settings = settingsStore.settingsFlow.first()
                val api = com.sergeapps.plants.data.api.PlantsApiFactory.create(settings)
                val repository = com.sergeapps.plants.data.PlantsRepository(api)
                repository.updateLocation(id = id, location = location, nbBin = nbBin, type = type)
            }.onSuccess {
                uiState.update { it.copy(isLoading = false, isEditDialogOpen = false, editId = null) }
                load()
            }.onFailure { ex ->
                uiState.update { it.copy(isLoading = false, error = ex.message ?: "Erreur") }
            }
        }
    }

    private fun refreshUsageCounts() {
        viewModelScope.launch {
            val locations = state.value.locations
            if (locations.isEmpty()) return@launch

            uiState.update { it.copy(isUsageLoading = true) }

            runCatching {
                val settings = settingsStore.settingsFlow.first()
                val api = PlantsApiFactory.create(settings)
                val repository = PlantsRepository(api)

                coroutineScope {
                    locations.map { dto ->
                        async {
                            val count = repository.getLocationUsageCount(dto.location)
                            dto.id to count
                        }
                    }.awaitAll().toMap()
                }
            }.onSuccess { map ->
                uiState.update { it.copy(isUsageLoading = false, usageCountById = map) }
            }.onFailure { ex ->
                // en cas d’erreur, on garde l’écran fonctionnel (pas de poubelle)
                uiState.update { it.copy(isUsageLoading = false, error = ex.message ?: "Erreur whereused") }
            }
        }
    }

    fun openDeleteConfirm(id: Int, name: String) {
        uiState.update {
            it.copy(
                isDeleteConfirmOpen = true,
                deleteCandidateId = id,
                deleteCandidateName = name
            )
        }
    }

    fun closeDeleteConfirm() {
        uiState.update { it.copy(isDeleteConfirmOpen = false, deleteCandidateId = null, deleteCandidateName = "") }
    }

    fun deleteConfirmed() {
        viewModelScope.launch {
            val id = state.value.deleteCandidateId ?: return@launch

            // Re-check rapide sécurité (optionnel mais recommandé)
            val name = state.value.deleteCandidateName
            uiState.update { it.copy(isLoading = true, error = null) }

            runCatching {
                val settings = settingsStore.settingsFlow.first()
                val api = PlantsApiFactory.create(settings)
                val repository = PlantsRepository(api)

                val usageCount = repository.getLocationUsageCount(name)
                if (usageCount > 0) {
                    throw IllegalStateException("Emplacement utilisé ($usageCount). Suppression impossible.")
                }

                repository.deleteLocation(id)
            }.onSuccess {
                uiState.update { it.copy(isLoading = false, isDeleteConfirmOpen = false, deleteCandidateId = null) }
                load()
            }.onFailure { ex ->
                uiState.update { it.copy(isLoading = false, error = ex.message ?: "Erreur suppression") }
            }
        }
    }
}