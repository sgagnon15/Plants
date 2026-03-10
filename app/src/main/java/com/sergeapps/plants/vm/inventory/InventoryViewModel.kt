package com.sergeapps.plants.vm.inventory

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sergeapps.plants.data.api.PlantsApiFactory
import com.sergeapps.plants.data.api.StockListRowDto
import com.sergeapps.plants.data.PlantsRepository
import com.sergeapps.plants.data.PlantsSettingsStore
import com.sergeapps.plants.data.api.LocationDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class InventoryUiState(
    val isLoading: Boolean = false,
    val error: String? = null,

    val locations: List<LocationDto> = emptyList(),
    val selectedLocation: LocationDto? = null,

    val items: List<StockListRowDto> = emptyList(),
    val isLoadingMore: Boolean = false,
    val canLoadMore: Boolean = false,
    val page: Int = 1
)

class InventoryViewModel(app: Application) : AndroidViewModel(app) {

    private val settingsStore = PlantsSettingsStore(app)
    private val uiState = MutableStateFlow(InventoryUiState(isLoading = true))
    val state: StateFlow<InventoryUiState> = uiState.asStateFlow()

    private val itemsPerPage: Int = 25

    init {
        loadLocations()
    }

    fun loadLocations() {
        viewModelScope.launch {
            try {
                uiState.value = uiState.value.copy(isLoading = true, error = null)

                val settings = settingsStore.settingsFlow.first()
                val api = PlantsApiFactory.create(settings)
                val repo = PlantsRepository(api)

                val locations = repo.loadLocations()

                uiState.value = uiState.value.copy(
                    isLoading = false,
                    locations = locations,
                    selectedLocation = null,
                    items = emptyList(),
                    page = 1,
                    canLoadMore = false
                )
            } catch (e: Exception) {
                uiState.value = uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Erreur chargement emplacements"
                )
            }
        }
    }

    fun selectLocation(location: LocationDto) {
        uiState.value = uiState.value.copy(
            selectedLocation = location,
            items = emptyList(),
            page = 1,
            canLoadMore = true,
            error = null
        )
        refresh()
    }

    fun refresh() {
        val selected = uiState.value.selectedLocation ?: return

        viewModelScope.launch {
            try {
                uiState.value = uiState.value.copy(isLoading = true, error = null)

                val settings = settingsStore.settingsFlow.first()
                val api = PlantsApiFactory.create(settings)
                val repo = PlantsRepository(api)

                val page1 = repo.loadStockListPage(
                    location = selected.location,
                    page = 1,
                    nbItems = itemsPerPage,
                    orderBy = "binnum"
                )

                uiState.value = uiState.value.copy(
                    isLoading = false,
                    items = page1,
                    page = 1,
                    canLoadMore = page1.isNotEmpty()
                )
            } catch (e: Exception) {
                uiState.value = uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Erreur chargement inventaire"
                )
            }
        }
    }

    fun loadMore() {
        val current = uiState.value
        val selected = current.selectedLocation ?: return
        if (current.isLoading || current.isLoadingMore || !current.canLoadMore) return

        viewModelScope.launch {
            try {
                uiState.value = uiState.value.copy(isLoadingMore = true, error = null)

                val settings = settingsStore.settingsFlow.first()
                val api = PlantsApiFactory.create(settings)
                val repo = PlantsRepository(api)

                val nextPage = current.page + 1

                val more = repo.loadStockListPage(
                    location = selected.location,
                    page = nextPage,
                    nbItems = itemsPerPage,
                    orderBy = "binnum"
                )

                uiState.value = uiState.value.copy(
                    isLoadingMore = false,
                    items = current.items + more,
                    page = nextPage,
                    canLoadMore = more.isNotEmpty()
                )
            } catch (e: Exception) {
                uiState.value = uiState.value.copy(
                    isLoadingMore = false,
                    error = e.message ?: "Erreur pagination inventaire"
                )
            }
        }
    }
}
