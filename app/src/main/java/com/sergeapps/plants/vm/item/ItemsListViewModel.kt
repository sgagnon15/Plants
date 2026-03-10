package com.sergeapps.plants.vm.item

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sergeapps.plants.data.api.PlantsApiFactory
import com.sergeapps.plants.data.PlantsRepository
import com.sergeapps.plants.data.PlantsSettingsStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.CancellationException

data class ItemsListUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val filter: String = "",
    val page: Int = 1,
    val pictureRotation: Int = 0,
    val totalPages: Int = 1,
    val items: List<ItemRowUi> = emptyList(),
    val isInitialLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val canLoadMore: Boolean = true
)

data class ItemRowUi(
    val id: Int,
    val itemNumber: String,
    val botanicalvar: String,
    val quantity: Double?,
    val origin: String,
    val commonname: String,
    val vendor: String,
    val imageUrl: String?,
    val thumbnailUrl: String?,
    val pictureRotation: Int
)

class ItemsListViewModel(app: Application) : AndroidViewModel(app) {
    private val settingsStore = PlantsSettingsStore(app)
    private val uiState = MutableStateFlow(ItemsListUiState(isLoading = true))
    val state: StateFlow<ItemsListUiState> = uiState.asStateFlow()
    private val filterFlow = MutableStateFlow("")
    private var refreshJob: Job? = null
    private var nextPageNumber: Int = 1
    private val itemsPerPage: Int = 15

    init {
        viewModelScope.launch {
            filterFlow
                .debounce(300)
                .distinctUntilChanged()
                .collectLatest { newFilter ->
                    uiState.value = uiState.value.copy(filter = newFilter, page = 1)
                    refresh()
                }
        }
    }

    fun refresh() {
        refreshJob?.cancel()

        refreshJob = viewModelScope.launch {
            try {
                nextPageNumber = 1

                uiState.value = uiState.value.copy(
                    isLoading = true,
                    isInitialLoading = true,
                    isLoadingMore = false,
                    canLoadMore = true,
                    error = null,
                    items = emptyList(),
                    page = 1,
                    totalPages = 1
                )

                val settings = settingsStore.settingsFlow.first()
                val api = PlantsApiFactory.create(settings)
                val repository = PlantsRepository(api)

                val currentFilter = uiState.value.filter

                val loadedItems = repository.loadItemsPage(
                    page = nextPageNumber,
                    nbItems = itemsPerPage,
                    filter = currentFilter
                )

                val mappedItems = loadedItems.map { dto ->
                    ItemRowUi(
                        id = dto.id,
                        itemNumber = dto.itemNumber.toString(),
                        botanicalvar = dto.botanicalVar,
                        commonname = dto.commonName,
                        quantity = dto.quantity,
                        origin = dto.origin,
                        vendor = dto.vendor.orEmpty(),
                        imageUrl = dto.url,
                        thumbnailUrl = dto.thumbnailUrl,
                        pictureRotation = dto.pictureRotation
                    )
                }

                val canLoadMore = loadedItems.size >= itemsPerPage

                uiState.value = uiState.value.copy(
                    isLoading = false,
                    isInitialLoading = false,
                    isLoadingMore = false,
                    canLoadMore = canLoadMore,
                    error = null,
                    items = mappedItems,
                    page = 1,
                    totalPages = 1
                )

                nextPageNumber = 2
            } catch (exception: Exception) {
                if (exception is CancellationException) {
                    return@launch
                }

                uiState.value = uiState.value.copy(
                    isLoading = false,
                    isInitialLoading = false,
                    isLoadingMore = false,
                    error = exception.message
                )
            }
        }
    }

    fun loadMore() {
        val currentState = uiState.value

        if (currentState.isLoadingMore) {
            return
        }

        if (!currentState.canLoadMore) {
            return
        }

        viewModelScope.launch {
            uiState.value = uiState.value.copy(
                isLoadingMore = true,
                error = null
            )

            try {
                val settings = settingsStore.settingsFlow.first()
                val api = PlantsApiFactory.create(settings)
                val repository = PlantsRepository(api)

                val currentFilter = uiState.value.filter

                val loadedItems = repository.loadItemsPage(
                    page = nextPageNumber,
                    nbItems = itemsPerPage,
                    filter = currentFilter
                )

                val mappedItems = loadedItems.map { dto ->
                    ItemRowUi(
                        id = dto.id,
                        itemNumber = dto.itemNumber.toString(),
                        botanicalvar = dto.botanicalVar,
                        commonname = dto.commonName,
                        quantity = dto.quantity,
                        origin = dto.origin,
                        vendor = dto.vendor.orEmpty(),
                        imageUrl = dto.url,
                        thumbnailUrl = dto.thumbnailUrl,
                        pictureRotation = dto.pictureRotation
                    )
                }

                val updatedList = currentState.items + mappedItems
                val canLoadMore = loadedItems.size >= itemsPerPage

                uiState.value = uiState.value.copy(
                    items = updatedList,
                    isLoadingMore = false,
                    canLoadMore = canLoadMore
                )

                if (canLoadMore) {
                    nextPageNumber += 1
                }
            } catch (exception: Exception) {
                if (exception is CancellationException) {
                    return@launch
                }

                uiState.value = uiState.value.copy(
                    isLoadingMore = false,
                    error = exception.message ?: "Erreur loadMore"
                )
            }
        }
    }

    fun onFilterChanged(newFilter: String) {
        uiState.value = uiState.value.copy(
            filter = newFilter,
            page = 1
        )
        filterFlow.value = newFilter
    }

    fun nextPage() {
        val next = (uiState.value.page + 1).coerceAtMost(uiState.value.totalPages)
        uiState.value = uiState.value.copy(page = next)
        refresh()
    }

    fun prevPage() {
        val prev = (uiState.value.page - 1).coerceAtLeast(1)
        uiState.value = uiState.value.copy(page = prev)
        refresh()
    }
}
