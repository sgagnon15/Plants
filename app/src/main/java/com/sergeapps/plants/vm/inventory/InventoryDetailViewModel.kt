package com.sergeapps.plants.vm.inventory

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sergeapps.plants.data.api.PlantsApiFactory
import com.sergeapps.plants.data.api.StockDetailDto
import com.sergeapps.plants.data.PlantsRepository
import com.sergeapps.plants.data.PlantsSettingsStore
import com.sergeapps.plants.data.api.LocationDto
import com.sergeapps.plants.data.api.StockUpsertRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


data class InventoryDetailUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val error: String? = null,
    val detail: StockDetailDto? = null,
    val editLocation: String = "",
    val editBinNum: String = "",
    val locations: List<LocationDto> = emptyList(),
    val isLoadingLocations: Boolean = false,
    val initialItemNumber: String = "",
    val stockTrans: StockTransUiState = StockTransUiState()
)

data class StockTransUiState(
    val isDialogOpen: Boolean = false,

    val isProjectsLoading: Boolean = false,
    val projectsError: String? = null,

    val projectId: String = "",
    val qtyText: String = "1",
    val transType: String = "ISSUE",
    val usage: String = "",

    val isPosting: Boolean = false,
    val postError: String? = null
)


class InventoryDetailViewModel(app: Application) : AndroidViewModel(app) {

    private val settingsStore = PlantsSettingsStore(app)

    private val uiState = MutableStateFlow(InventoryDetailUiState())
    val state: StateFlow<InventoryDetailUiState> = uiState.asStateFlow()
    private var repository: PlantsRepository? = null

    fun load(stockId: Int, initialItemNumber: String = "") {
        viewModelScope.launch {
            try {
                uiState.update { it.copy(isLoading = true, error = null) }

                val settings = settingsStore.settingsFlow.first()
                val api = PlantsApiFactory.create(settings)
                repository = PlantsRepository(api)

                val repo = PlantsRepository(api)
                val detail = repo.loadStockDetail(stockId = stockId, itemNumber = initialItemNumber)

                uiState.update {
                    it.copy(
                        isLoading = false,
                        detail = detail,
                        initialItemNumber = detail.itemNumber, //initialItemNumber,
                        editLocation = detail.location.orEmpty(),
                        editBinNum = detail.binNum.orEmpty(),
                        error = null
                    )
                }

                loadLocations()
                return@launch
            } catch (e: Exception) {
                uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Erreur chargement détail"
                    )
                }
            }
        }
    }

    fun startEditing() {
        val detail = uiState.value.detail ?: return
        uiState.value = uiState.value.copy(
            editLocation = detail.location,
            editBinNum = detail.binNum,
            error = null
        )
    }

    fun updateBinNum(value: String) {
        uiState.value = uiState.value.copy(editBinNum = value)
    }

    fun saveChanges() {
        val current = uiState.value

        val newLocation = current.editLocation.trim()
        val newBinNum = current.editBinNum.trim()

        if (newLocation.isBlank()) {
            uiState.value = current.copy(error = "Emplacement invalide")
            return
        }

        if (newBinNum.isBlank()) {
            uiState.value = current.copy(error = "Binnum invalide")
            return
        }

        val isNewStock = (current.detail == null)
        val itemNumberToSave = if (isNewStock) current.initialItemNumber else current.detail!!.itemNumber
        val stockIdToSave = if (isNewStock) 0 else current.detail!!.stockId

        if (isNewStock) {
            uiState.value = current.copy(error = "No. article invalide")
            return
        }

        viewModelScope.launch {
            try {
                uiState.value = current.copy(isSaving = true, error = null)

                val settings = settingsStore.settingsFlow.first()
                val api = PlantsApiFactory.create(settings)
                val repo = PlantsRepository(api)

                val body = StockUpsertRequest(
                    itemNumber = itemNumberToSave,
                    location = newLocation,
                    binNum = newBinNum
                )

                val returnedStockId = repo.upsertStock(stockId = stockIdToSave, body = body)
                val finalStockId = if (returnedStockId > 0) returnedStockId else stockIdToSave
                val refreshed = repo.loadStockDetail(finalStockId, itemNumberToSave)

                uiState.value = current.copy(
                    isSaving = false,
                    detail = refreshed,
                    // une fois créé, tu peux remettre initialItemNumber à 0 ou garder
                    initialItemNumber = refreshed.itemNumber,
                    editLocation = refreshed.location.orEmpty(),
                    editBinNum = refreshed.binNum.orEmpty(),
                    error = null
                )
            } catch (e: Exception) {
                uiState.value = current.copy(
                    isSaving = false,
                    error = e.message ?: "Erreur sauvegarde"
                )
            }
        }
    }

    fun loadLocations() {
        viewModelScope.launch {
            try {
                uiState.update { it.copy(isLoadingLocations = true, error = null) }

                val settings = settingsStore.settingsFlow.first()
                val api = PlantsApiFactory.create(settings)
                val repo = PlantsRepository(api)

                val locations = repo.loadLocations()

                uiState.update {
                    it.copy(
                        isLoadingLocations = false,
                        locations = locations
                    )
                }
            } catch (e: Exception) {
                uiState.update {
                    it.copy(
                        isLoadingLocations = false,
                        error = e.message ?: "Erreur chargement emplacements"
                    )
                }
            }
        }
    }

    fun selectLocation(locationName: String) {
        uiState.value = uiState.value.copy(editLocation = locationName)
    }

    fun deleteStock(stockId: Int, onSuccess: () -> Unit) {
        viewModelScope.launch {
            uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val settings = settingsStore.settingsFlow.first()
                val api = PlantsApiFactory.create(settings)
                val repository = PlantsRepository(api)

                repository.deleteStock(stockId)

                uiState.update { it.copy(isLoading = false) }
                onSuccess()
            } catch (e: Exception) {
                uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Erreur lors de la suppression"
                    )
                }
            }
        }
    }

    fun openStockTransDialog() {
        uiState.update {
            it.copy(
                stockTrans = it.stockTrans.copy(
                    isDialogOpen = true,
                    postError = null
                )
            )
        }
    }

    fun closeStockTransDialog() {
        uiState.update {
            it.copy(
                stockTrans = it.stockTrans.copy(
                    isDialogOpen = false,
                    postError = null
                )
            )
        }
    }
}
