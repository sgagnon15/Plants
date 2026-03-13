package com.sergeapps.plants.vm.control

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sergeapps.plants.data.PlantsSettingsStore
import com.sergeapps.plants.data.api.PlantsApiFactory
import com.sergeapps.plants.data.api.PlantsApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
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
    val controllerName: String = "",
    val macAddress: String = "",
    val zone: String = "Zone 1",
    val availableZones: List<String> = listOf("Zone 1", "Zone 2", "Zone 3"),
    val currentStatus: String = "",
    val waterFlow: String = "0",
    val scheduleRows: List<ScheduleRowUi> = emptyList(),
    val historyRows: List<HistoryRowUi> = emptyList(),
    val generalParams: GeneralParamsUi = GeneralParamsUi(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class ControlViewModel(app: Application) : AndroidViewModel(app) {

    private val settingsStore = PlantsSettingsStore(app)
    private var api: PlantsApiService? = null

    private val uiState = MutableStateFlow(ControlUiState())
    val state: StateFlow<ControlUiState> = uiState.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                val settings = settingsStore.settingsFlow.first()

                api = PlantsApiFactory.create(settings)
                loadControlData()
            } catch (exception: Exception) {
                uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Erreur d'initialisation"
                    )
                }
            }
        }
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
                val service = api ?: throw IllegalStateException("API non initialisée")

                val controllers = service.getControllerList()
                val controller = controllers.firstOrNull()
                    ?: throw IllegalStateException("Aucun contrôleur trouvé")

                val controllerName = controller.controlername.toString()
                val macAddress = controller.macaddress.toString()

                val infoList = service.getInfo(macAddress)
                val info = infoList.firstOrNull()

                val chrono = service.getChrono(macAddress)
                val genParam = service.getGenParam(controllerName)
                val schedules = service.getScheduleList(
                    macAddress = macAddress,
                    zone = uiState.value.zone
                )

                val autoStart = "%02d:%02d".format(
                    genParam.wateringStartHour,
                    genParam.wateringStartMin
                )

                uiState.update {
                    it.copy(
                        controllerName = controllerName,
                        macAddress = macAddress,
                        currentStatus = if (chrono.remain > 0) {
                            "Arrosage en cours"
                        } else {
                            "Arrêt"
                        },
                        waterFlow = chrono.waterFlow.toString(),
                        scheduleRows = schedules.map { schedule ->
                            ScheduleRowUi(
                                id = schedule.id,
                                startTime = schedule.starttime,
                                duration = schedule.duration.toString(),
                                fertilizer = false
                            )
                        },
                        generalParams = GeneralParamsUi(
                            autoStart = autoStart,
                            waterDuration = genParam.wateringDuration.toString(),
                            manualDuration = genParam.manualDuration.toString(),
                            feedEnabled =
                            genParam.feeding == "1" ||
                                    genParam.feeding.equals("true", ignoreCase = true) ||
                                    genParam.feeding.equals("oui", ignoreCase = true),
                            feedDuration = genParam.feedDuration,
                            updateFrequency = genParam.updatefreq.toString()
                        ),
                        historyRows = listOf(
                            HistoryRowUi(
                                date = info?.runningSince ?: "",
                                state = info?.autoWatering ?: "",
                                flow = chrono.waterFlow.toString()
                            )
                        ),
                        isLoading = false
                    )
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
        uiState.update { it.copy(zone = zone) }
        loadControlData()
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
                    if (row.id == rowId) updatedRow else row
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
        uiState.update {
            it.copy(currentStatus = "Arrosage manuel")
        }
    }

    fun onFeedClick() {
        uiState.update {
            it.copy(currentStatus = "Fertilisation")
        }
    }

    fun onSearchHistory() {
    }

    fun saveAll() {
    }
}