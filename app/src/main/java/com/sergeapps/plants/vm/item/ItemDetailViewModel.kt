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

import android.content.Context
import android.net.Uri
import com.sergeapps.plants.data.api.ItemDetailDto
import kotlinx.coroutines.flow.update

import androidx.core.content.FileProvider
import com.sergeapps.plants.ui.item.InventoryRowUi
import java.io.File


private var pendingCameraPhotoUri: Uri? = null

data class VendorUi(val name: String)

data class UpdateItemDetailDto(
    val id: Int,
    val botanicalVar: String,
    val cultivar: String,
    val commonName: String,
    val wiki: String,
    val origin: String,
    val light: String,
    val soil: String,
    val water: String,
    val temperatureMin: Int?,
    val temperatureMax: Int?,
    val dormancy: String,
    val feed: String
)

data class ItemDetailUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val saveError: String? = null,
    val error: String? = null,
    val isNewItem: Boolean = false,
    val itemDetail: ItemDetailDto? = null,
    val itemId: Int = 0,

    // Champs éditables (création / édition)
    val itemNumberText: String = "",
    val botanicalvarText: String = "",
    val commonnameText: String = "",
    val cultivarText: String = "",
    val originText: String = "",
    val vendorUrlText: String = "",
    val creationDateText: String = "",

    // Care instruction
    val light: String? = null,
    val soil: String? = null,
    val water: String? = null,
    val feed: String? = null,
    val dormancy: String? = null,
    val transplant: String? = null,
    val otherCare: String? = null,
    val humidity: String? = null,
    val temperature: String? = null,

    // --- PHOTO ---
    val imageUrl: String? = null,
    val wikiText: String? = null,
    val thumbnailUrl: String? = null,
    val localSelectedPhotoUri: Uri? = null,
    val isUploadingPhoto: Boolean = false,
    val photoVersion: Long = 0L,

    val apiKey: String = "",
    val vendorText: String = "",
    val vendorOptions: List<VendorUi> = emptyList(),
    val isVendorLoading: Boolean = false,
    val quantity: Double = 0.0,
    val pictureRotation: Int = 0,
    val inventoryByLocation: List<InventoryRowUi> = emptyList(),
    val isInventoryByLocationLoading: Boolean = false
)

data class ManufUi(val name: String)

class ItemDetailViewModel(app: Application) : AndroidViewModel(app) {

    private val settingsStore = PlantsSettingsStore(app)
    private lateinit var repository: PlantsRepository


    private val uiState = MutableStateFlow(ItemDetailUiState())
    val state: StateFlow<ItemDetailUiState> = uiState.asStateFlow()

    private val _loadingCareAi = MutableStateFlow(false)
    val loadingCareAi: StateFlow<Boolean> = _loadingCareAi


    fun load(itemId: Int) {
        viewModelScope.launch {
            uiState.update { it.copy(isLoading = true, error = null) }

            val settings = settingsStore.settingsFlow.first()
            val api = PlantsApiFactory.create(settings)
            repository = PlantsRepository(api)

            if (itemId == 0) {

                val nextItemNumber: Int = try {
                    repository.getNextItemNumber()
                } catch (_: Exception) {
                    0
                }
                uiState.value = ItemDetailUiState(
                    isLoading = false,
                    isNewItem = true,
                    itemId = 0,
                    apiKey = settings.apiKey,
                    itemNumberText = if (nextItemNumber > 0) nextItemNumber.toString() else "",
                    botanicalvarText = "",
                    quantity = 0.0,
                    commonnameText = "",
                    vendorText = "",
                    wikiText = ""
                )
                return@launch
            }

            runCatching {
                repository.loadItemDetail(itemId)
            }.onSuccess { dto ->
                uiState.value = uiState.value.copy(
                    isLoading = false,
                    isNewItem = false,
                    itemId = itemId,
                    itemDetail = dto,
                    imageUrl = dto.url,
                    thumbnailUrl = dto.thumbnailurl,
                    apiKey = settings.apiKey,
                    vendorUrlText = uiState.value.vendorUrlText.ifBlank { dto.vendorUrl.orEmpty() },
                    itemNumberText = dto.itemNumber?.toString().orEmpty(),
                    botanicalvarText = dto.botanicalVar.orEmpty(),
                    quantity = dto.quantity?.toDouble() ?: 0.0,
                    commonnameText = dto.commonName.orEmpty(),
                    cultivarText = dto.cultivar.orEmpty(),
                    originText = dto.origin.orEmpty(),
                    wikiText = dto.wiki.orEmpty(),
                    vendorText = dto.vendor.orEmpty(),
                    creationDateText = dto.creationDate.orEmpty(),
                    light = dto.light,
                    soil = dto.soil,
                    water = dto.water,
                    feed = dto.feed,
                    dormancy = dto.dormancy,
                    humidity = dto.humidity,
                    transplant = dto.transplant,
                    otherCare = dto.otherCare,
                    temperature = dto.temperature
                )

                dto.itemNumber?.let { itemNumber ->
                    if (itemNumber > "") {
                        loadInventoryByItemNumber(itemNumber.toString())
                    }
                }
            }.onFailure { e ->
                uiState.value = ItemDetailUiState(
                    isLoading = false,
                    error = e.message ?: "Erreur réseau"
                )
            }
        }
    }

    fun onPickPhoto(uri: Uri) {
        uiState.update {
            it.copy(localSelectedPhotoUri = uri)
        }
    }

    fun uploadPickedPhoto(context: Context) {
        val itemId = uiState.value.itemId
        if (itemId == 0) return

        val uri = uiState.value.localSelectedPhotoUri ?: return

        viewModelScope.launch {
            uiState.update { it.copy(isUploadingPhoto = true, error = null) }

            runCatching {
                repository.uploadPhoto(context, itemId, uri)
            }.onSuccess { response ->
                if (response.ok && !response.url.isNullOrBlank()) {
                    uiState.update {
                        it.copy(
                            imageUrl = response.url,
                            localSelectedPhotoUri = null,
                            isUploadingPhoto = false,
                            error = null,
                            photoVersion = System.currentTimeMillis()
                        )
                    }
                } else {
                    uiState.update {
                        it.copy(
                            isUploadingPhoto = false,
                            error = response.error ?: "Upload échoué"
                        )
                    }
                }
            }.onFailure { e ->
                uiState.update {
                    it.copy(
                        isUploadingPhoto = false,
                        error = e.message ?: "Erreur upload photo"
                    )
                }
            }
        }
    }

    fun deletePhoto() {
        val itemId = uiState.value.itemId
        if (itemId == 0) return

        val pictureUrl = uiState.value.imageUrl ?: return
        if (!::repository.isInitialized) return
        val repo = repository

        viewModelScope.launch {
            uiState.update { it.copy(isUploadingPhoto = true, error = null) }

            runCatching {
                repo.deletePhoto(itemId, pictureUrl)
            }.onSuccess {
                uiState.update {
                    it.copy(
                        imageUrl = null,
                        localSelectedPhotoUri = null,
                        isUploadingPhoto = false,
                        error = null,
                        photoVersion = System.currentTimeMillis()
                    )
                }
            }.onFailure { e ->
                uiState.update {
                    it.copy(
                        imageUrl = null,
                        localSelectedPhotoUri = null,
                        isUploadingPhoto = false
                    )
                }
            }
        }
    }

    fun createTempPhotoUri(context: Context): Uri {
        val photoFile = File(
            context.cacheDir,
            "item_photo_${System.currentTimeMillis()}.jpg"
        )

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            photoFile
        )

        pendingCameraPhotoUri = uri
        return uri
    }

    private fun loadInventoryByItemNumber(
        itemNumber: String
    ) {
        viewModelScope.launch {

            uiState.update {
                it.copy(isInventoryByLocationLoading = true)
            }

            runCatching {
                repository.fetchInventoryByItemNumber(itemNumber)
            }.onSuccess { list ->

                uiState.update {
                    it.copy(
                        inventoryByLocation = list.sortedWith(
                            compareBy<InventoryRowUi> { it.location }
                                .thenBy { it.position ?: "" }
                        ),
                        isInventoryByLocationLoading = false
                    )
                }

            }.onFailure {

                uiState.update {
                    it.copy(
                        inventoryByLocation = emptyList(),
                        isInventoryByLocationLoading = false
                    )
                }
            }
        }
    }

    fun onItemNumberChanged(value: String) {
        uiState.update { it.copy(itemNumberText = value) }
    }

    fun onDescriptionChanged(value: String) {
        uiState.update { it.copy(botanicalvarText = value) }
    }

    suspend fun save(): Comparable<Nothing> {

        uiState.update {
            it.copy(
                isSaving = true,
                saveSuccess = false,
                saveError = null,
                error = null
            )
        }

        val result = try {

            val currentState = uiState.value

            val itemNumber = currentState.itemNumberText.trim().toIntOrNull()
                ?: throw IllegalArgumentException("No. article invalide")

            val botanicalvar = currentState.botanicalvarText.trim()
                .ifBlank { throw IllegalArgumentException("Description requise") }

            val vendor = currentState.vendorText.trim()
            val vendorUrl = currentState.vendorUrlText.trim().ifBlank { null }

            val createdOrUpdatedId = if (currentState.isNewItem) {

                repository.createItem(
                    itemNumber = itemNumber.toString(),
                    botanicalvar = botanicalvar,
                    commonName = currentState.commonnameText.trim(),
                    cultivar = currentState.cultivarText.trim(),
                    origin = currentState.originText.trim(),
                    wiki = currentState.wikiText?.trim().orEmpty(),
                    light = currentState.light?.trim().orEmpty(),
                    soil = currentState.soil?.trim().orEmpty(),
                    water = currentState.water?.trim().orEmpty(),
                    temperature = currentState.temperature?.trim().orEmpty(),
                    dormancy = currentState.dormancy?.trim().orEmpty(),
                    feed = currentState.feed?.trim().orEmpty()
                )

            } else {

                val existingId = currentState.itemId
                if (existingId == 0) {
                    throw IllegalStateException("itemId manquant")
                }

                repository.updateItem(
                    itemId = existingId,
                    itemNumber = itemNumber.toString(),
                    botanicalvar = botanicalvar,
                    commonName = currentState.commonnameText.trim(),
                    cultivar = currentState.cultivarText.trim(),
                    origin = currentState.originText.trim(),
                    wiki = currentState.wikiText?.trim().orEmpty(),
                    light = currentState.light?.trim().orEmpty(),
                    soil = currentState.soil?.trim().orEmpty(),
                    water = currentState.water?.trim().orEmpty(),
                    temperature = currentState.temperature?.trim().orEmpty(),
                    dormancy = currentState.dormancy?.trim().orEmpty(),
                    feed = currentState.feed?.trim().orEmpty()
                )

                existingId
            }

            uiState.update {
                it.copy(
                    isSaving = false,
                    saveSuccess = true,
                    saveError = null
                )
            }

            createdOrUpdatedId

        } catch (e: Exception) {

            uiState.update {
                it.copy(
                    isSaving = false,
                    saveSuccess = false,
                    saveError = e.message,
                    error = e.message
                )
            }

            throw e
        }

        return result
    }

    fun onVendorUrlChanged(value: String) {
        uiState.update { it.copy(vendorUrlText = value) }
    }

    fun onCommonNameChanged(value: String) {
        uiState.update { it.copy(commonnameText = value) }
    }

    fun onCultivarChanged(value: String) {
        uiState.update { it.copy(cultivarText = value) }
    }

    fun onOriginChanged(value: String) {
        uiState.update { it.copy(originText = value) }
    }

    fun onWikiChanged(value: String) {
        uiState.update { it.copy(wikiText = value) }
    }

    fun onQuantityChanged(value: String) {
        uiState.update {
            it.copy(
                quantity = value.toDoubleOrNull() ?: 0.0
            )
        }
    }

    fun fillCareInstructionsWithAI(plantName: String) {
        viewModelScope.launch {
            try {
                _loadingCareAi.value = true

                val care = repository.getPlantCare(plantName)
                val current = uiState.value

                uiState.value = current.copy(
                    water = care.arrosage ?: current.water,
                    light = care.lumière ?: current.light,
                    temperature = care.température ?: current.temperature,
                    feed = care.engrais ?: current.feed,
                    soil = care.substrat ?: current.soil,
                    dormancy = care.dormance ?: current.dormancy,
                    transplant = care.rempotage ?: current.transplant,
                    humidity = care.humidité ?: current.humidity
                )
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _loadingCareAi.value = false
            }
        }
    }

    fun saveChanges(
        botanicalVar: String,
        cultivar: String,
        commonName: String,
        wiki: String,
        origin: String,
        light: String,
        soil: String,
        water: String,
        temperatureMin: String,
        temperatureMax: String,
        dormancy: String,
        feed: String
    ) {
        viewModelScope.launch {
            uiState.update {
                it.copy(
                    isSaving = true,
                    saveSuccess = false,
                    saveError = null
                )
            }

            val payload = UpdateItemDetailDto(
                id = uiState.value.itemId,
                botanicalVar = botanicalVar.trim(),
                cultivar = cultivar.trim(),
                commonName = commonName.trim(),
                wiki = wiki.trim(),
                origin = origin.trim(),
                light = light.trim(),
                soil = soil.trim(),
                water = water.trim(),
                temperatureMin = temperatureMin.toIntOrNull(),
                temperatureMax = temperatureMax.toIntOrNull(),
                dormancy = dormancy.trim(),
                feed = feed.trim()
            )

            val result = repository.updateItemDetail(payload)

            result
                .onSuccess {
                    uiState.update {
                        it.copy(
                            botanicalvarText = payload.botanicalVar,
                            cultivarText = payload.cultivar,
                            commonnameText = payload.commonName,
                            wikiText = payload.wiki,
                            originText = payload.origin,
                            light = payload.light,
                            soil = payload.soil,
                            water = payload.water,
                            dormancy = payload.dormancy,
                            feed = payload.feed,
                            isSaving = false,
                            saveSuccess = true,
                            saveError = null
                        )
                    }
                }
                .onFailure { exception ->
                    uiState.update {
                        it.copy(
                            isSaving = false,
                            saveSuccess = false,
                            saveError = exception.message ?: "Erreur inconnue"
                        )
                    }
                }
        }
    }

    fun onLightChanged(value: String) {
        uiState.update { it.copy(light = value) }
    }

    fun onSoilChanged(value: String) {
        uiState.update { it.copy(soil = value) }
    }

    fun onWaterChanged(value: String) {
        uiState.update { it.copy(water = value) }
    }

    fun onTemperatureChanged(value: String) {
        uiState.update { it.copy(temperature = value) }
    }

    fun onDormancyChanged(value: String) {
        uiState.update { it.copy(dormancy = value) }
    }

    fun onFeedChanged(value: String) {
        uiState.update { it.copy(feed = value) }
    }
}