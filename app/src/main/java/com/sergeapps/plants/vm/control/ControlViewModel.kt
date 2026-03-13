package com.sergeapps.plants.vm.control

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sergeapps.plants.data.PlantsRepository
import com.sergeapps.plants.data.PlantsSettings
import com.sergeapps.plants.data.PlantsSettingsStore
import com.sergeapps.plants.data.api.PlantsApiFactory
import com.sergeapps.plants.vm.inventory.InventoryDetailUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ScheduleRowUi(
    val id: Int,
    val startTime: String = "",
    val duration: String = "",
    val fertilizer: Boolean = false
)

data class HistoryRowUi(
    val date: String,
    val state: String,
    val flow: String
)

data class GeneralParamsUi(
    val autoStart: String = "",
    val waterDuration: String = "",
    val manualDuration: String = "",
    val feedEnabled: Boolean = false,
    val feedDuration: String = "",
    val updateFrequency: String = ""
)

data class ControlUiState(
    val zone: String = "",
    val availableZones: List<String> = emptyList(),
    val currentStatus: String = "Arrosage en cours",
    val waterFlow: String = "0.00",
    val scheduleRows: List<ScheduleRowUi> = emptyList(),
    val historyRows: List<HistoryRowUi> = emptyList(),
    val generalParams: GeneralParamsUi = GeneralParamsUi(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class ControlViewModel(app: Application) : AndroidViewModel(app) {
    private val settingsStore = PlantsSettingsStore(app)
    private var repository: PlantsRepository? = null


    private val uiState = MutableStateFlow(
        ControlUiState(
            zone = "Zone 1",
            availableZones = listOf("Zone 1", "Zone 2", "Zone 3"),
            scheduleRows = listOf(
                ScheduleRowUi(id = 1, startTime = "08:00", duration = "30", fertilizer = false),
                ScheduleRowUi(id = 2, startTime = "18:00", duration = "20", fertilizer = true)
            ),
            historyRows = listOf(
                HistoryRowUi(date = "2026-03-12", state = "Arrosage", flow = "120 ml"),
                HistoryRowUi(date = "2026-03-11", state = "Fertilisation", flow = "50 ml"),
                HistoryRowUi(date = "2026-03-10", state = "Arrosage", flow = "110 ml")
            ),
            generalParams = GeneralParamsUi(
                autoStart = "07:00",
                waterDuration = "30 sec",
                manualDuration = "20 sec",
                feedEnabled = true,
                feedDuration = "10 sec",
                updateFrequency = "15 min"
            )
        )
    )
    val state: StateFlow<ControlUiState> = uiState.asStateFlow()

    init {
        loadControlData()
    }

    fun loadControlData() {
        viewModelScope.launch {
            uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null
                )
            }

            try {
                // TODO:
                // Remplacer ce bloc par les appels réels repository/api
                //
                // Exemple futur :
                // val controlDto = repository.getControlData()
                // uiState.update { old ->
                //     old.copy(
                //         zone = controlDto.zone,
                //         availableZones = controlDto.availableZones,
                //         currentStatus = controlDto.currentStatus,
                //         waterFlow = controlDto.waterFlow,
                //         scheduleRows = controlDto.scheduleRows.map { ... },
                //         historyRows = controlDto.historyRows.map { ... },
                //         generalParams = ...
                //     )
                // }

                uiState.update {
                    it.copy(isLoading = false)
                }
            } catch (exception: Exception) {
                uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Erreur de chargement"
                    )
                }
            }
        }
    }

    fun onZoneChange(zone: String) {
        uiState.update {
            it.copy(zone = zone)
        }
    }

    fun addScheduleRow() {
        uiState.update { currentState ->
            val nextId = (currentState.scheduleRows.maxOfOrNull { it.id } ?: 0) + 1
            currentState.copy(
                scheduleRows = currentState.scheduleRows + ScheduleRowUi(id = nextId)
            )
        }
    }

    fun updateScheduleRow(
        rowId: Int,
        updatedRow: ScheduleRowUi
    ) {
        uiState.update { currentState ->
            currentState.copy(
                scheduleRows = currentState.scheduleRows.map { row ->
                    if (row.id == rowId) {
                        updatedRow
                    } else {
                        row
                    }
                }
            )
        }
    }

    fun deleteScheduleRow(rowId: Int) {
        uiState.update { currentState ->
            currentState.copy(
                scheduleRows = currentState.scheduleRows.filterNot { it.id == rowId }
            )
        }
    }

    fun onWaterClick() {
        viewModelScope.launch {
            // TODO brancher appel backend arrosage manuel
            uiState.update {
                it.copy(currentStatus = "Arrosage manuel")
            }
        }
    }

    fun onFeedClick() {
        viewModelScope.launch {
            // TODO brancher appel backend fertilisation
            uiState.update {
                it.copy(currentStatus = "Fertilisation")
            }
        }
    }

    fun onSearchHistory() {
        viewModelScope.launch {
            // TODO brancher recherche historique
        }
    }

    fun saveAll() {
        viewModelScope.launch {
            try {
                // TODO envoyer au backend :
                // zone
                // scheduleRows
                // generalParams
            } catch (exception: Exception) {
                uiState.update {
                    it.copy(errorMessage = exception.message ?: "Erreur de sauvegarde")
                }
            }
        }
    }
}