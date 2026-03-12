package com.sergeapps.plants.vm.batchtransfer

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
import kotlinx.coroutines.runBlocking


data class BatchTransferRow(
    val stockId: Int,
    val itemNumber: String,
    val botanicalVar: String,
    val cultivar: String,
    val specimenNumber: String,
    val currentLocation: String,
    val currentPosition: String,
    val isChecked: Boolean = false
)

data class BatchTransferUiState(
    val isLoading: Boolean = false,
    val isExecuting: Boolean = false,
    val sourceLocation: String = "",
    val sourcePosition: String = "",
    val destinationLocation: String = "",
    val destinationPosition: String = "",
    val locations: List<LocationDto> = emptyList(),
    val sourcePositions: List<String> = emptyList(),
    val destinationPositions: List<String> = emptyList(),
    val rows: List<BatchTransferRow> = emptyList(),
    val errorMessage: String? = null,
    val successMessage: String? = null
) {
    val availableDestinationLocations: List<LocationDto>
        get() = locations.filter { it.location != sourceLocation }

    val selectedCount: Int
        get() = rows.count { it.isChecked }

    val canExecute: Boolean
        get() = sourceLocation.isNotBlank() &&
                sourcePosition.isNotBlank() &&
                destinationLocation.isNotBlank() &&
                destinationPosition.isNotBlank() &&
                (sourceLocation != destinationLocation || sourcePosition != destinationPosition) &&
                selectedCount > 0 &&
                !isExecuting
}


class BatchTransferViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val settingsStore = PlantsSettingsStore(application)

    private val api by lazy {
        val settings = runBlocking {
            settingsStore.settingsFlow.first()
        }
        PlantsApiFactory.create(settings)
    }

    private val repository by lazy {
        PlantsRepository(api)
    }

    private val uiState = MutableStateFlow(BatchTransferUiState())
    val state: StateFlow<BatchTransferUiState> = uiState.asStateFlow()

    init {
        loadLocations()
    }
    fun clearMessage() {
        uiState.update {
            it.copy(
                errorMessage = null,
                successMessage = null
            )
        }
    }

    fun loadLocations() {
        viewModelScope.launch {
            uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null
                )
            }

            try {
                val locations = repository.getLocations()

                uiState.update {
                    it.copy(
                        locations = locations,
                        isLoading = false
                    )
                }
            } catch (exception: Exception) {
                uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Erreur lors du chargement des emplacements"
                    )
                }
            }
        }
    }

    fun toggleRow(stockId: Int) {
        uiState.update { current ->
            current.copy(
                rows = current.rows.map { row ->
                    if (row.stockId == stockId) {
                        row.copy(isChecked = !row.isChecked)
                    } else {
                        row
                    }
                }
            )
        }
    }

    fun toggleSelectAll(selectAll: Boolean) {
        uiState.update { current ->
            current.copy(
                rows = current.rows.map { row ->
                    row.copy(isChecked = selectAll)
                }
            )
        }
    }

    private fun loadStockForSource(
        location: String,
        position: String
    ) {
        viewModelScope.launch {
            uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null
                )
            }

            try {
                val stockList = repository.loadStockListPage(
                    location = location,
                    page = 1,
                    nbItems = 9999
                )

                val filteredList = stockList.filter { stock ->
                    stock.position.orEmpty().trim() == position.trim()
                }

                val rows = filteredList.map { stock ->
                    BatchTransferRow(
                        stockId = stock.stockId,
                        itemNumber = stock.itemNumber.orEmpty(),
                        botanicalVar = stock.botanicalvar.orEmpty(),
                        cultivar = stock.cultivar.orEmpty(),
                        specimenNumber = stock.specimenNumber?.toString().orEmpty(),
                        currentLocation = stock.location.orEmpty(),
                        currentPosition = stock.position.orEmpty(),
                        isChecked = false
                    )
                }

                uiState.update {
                    it.copy(
                        rows = rows,
                        isLoading = false
                    )
                }
            } catch (exception: Exception) {
                uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Erreur lors du chargement des plantes"
                    )
                }
            }
        }
    }

    fun executeTransfer() {
        val currentState = uiState.value
        val destinationLocation = currentState.destinationLocation
        val destinationPosition = currentState.destinationPosition
        val selectedRows = currentState.rows.filter { it.isChecked }

        if (currentState.sourceLocation.isBlank()) {
            uiState.update { it.copy(errorMessage = "Choisis un emplacement source") }
            return
        }

        if (currentState.sourcePosition.isBlank()) {
            uiState.update { it.copy(errorMessage = "Choisis une position source") }
            return
        }

        if (destinationLocation.isBlank()) {
            uiState.update { it.copy(errorMessage = "Choisis un emplacement de destination") }
            return
        }

        if (destinationPosition.isBlank()) {
            uiState.update { it.copy(errorMessage = "Choisis une position de destination") }
            return
        }

        if (currentState.sourceLocation == destinationLocation &&
            currentState.sourcePosition == destinationPosition
        ) {
            uiState.update {
                it.copy(errorMessage = "La destination doit être différente de la source")
            }
            return
        }

        if (selectedRows.isEmpty()) {
            uiState.update { it.copy(errorMessage = "Aucune plante sélectionnée") }
            return
        }

        viewModelScope.launch {
            uiState.update {
                it.copy(
                    isExecuting = true,
                    errorMessage = null,
                    successMessage = null
                )
            }

            try {
                selectedRows.forEach { row ->
                    repository.moveStockLocation(
                        stockId = row.stockId,
                        location = destinationLocation,
                        position = destinationPosition
                    )
                }

                uiState.update {
                    it.copy(
                        isExecuting = false,
                        successMessage = "${selectedRows.size} plante(s) transférée(s)"
                    )
                }

                loadStockForSource(
                    location = uiState.value.sourceLocation,
                    position = uiState.value.sourcePosition
                )

            } catch (exception: Exception) {
                uiState.update {
                    it.copy(
                        isExecuting = false,
                        errorMessage = exception.message ?: "Erreur lors du transfert"
                    )
                }
            }
        }
    }
    private fun loadSourcePositions(location: String) {
        viewModelScope.launch {
            try {
                val positions = repository.loadPositions(location)

                uiState.update {
                    it.copy(sourcePositions = positions)
                }
            } catch (exception: Exception) {
                uiState.update {
                    it.copy(
                        errorMessage = exception.message ?: "Erreur lors du chargement des positions source"
                    )
                }
            }
        }
    }

    private fun loadDestinationPositions(location: String) {
        viewModelScope.launch {
            try {
                val positions = repository.loadPositions(location)

                uiState.update {
                    it.copy(destinationPositions = positions)
                }
            } catch (exception: Exception) {
                uiState.update {
                    it.copy(
                        errorMessage = exception.message ?: "Erreur lors du chargement des positions de destination"
                    )
                }
            }
        }
    }

    fun onSourceLocationSelected(location: String) {
        uiState.update {
            it.copy(
                sourceLocation = location,
                sourcePosition = "",
                sourcePositions = emptyList(),
                rows = emptyList(),
                errorMessage = null,
                successMessage = null
            )
        }

        if (uiState.value.destinationLocation == location &&
            uiState.value.destinationPosition == uiState.value.sourcePosition
        ) {
            uiState.update {
                it.copy(destinationPosition = "")
            }
        }

        loadSourcePositions(location)
    }

    fun onDestinationLocationSelected(location: String) {
        uiState.update {
            it.copy(
                destinationLocation = location,
                destinationPosition = "",
                destinationPositions = emptyList(),
                errorMessage = null,
                successMessage = null
            )
        }

        loadDestinationPositions(location)
    }

    fun onSourcePositionSelected(position: String) {
        uiState.update {
            it.copy(
                sourcePosition = position,
                rows = emptyList(),
                errorMessage = null,
                successMessage = null
            )
        }

        loadStockForSource(
            location = uiState.value.sourceLocation,
            position = position
        )
    }

    fun onDestinationPositionSelected(position: String) {
        uiState.update {
            it.copy(
                destinationPosition = position,
                errorMessage = null,
                successMessage = null
            )
        }
    }
}

