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
import kotlinx.serialization.SerialName
import java.io.File


private var pendingCameraPhotoUri: Uri? = null

data class VendorUi(val name: String)

data class ItemDetailUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val isNewItem: Boolean = false,
    val itemDetail: ItemDetailDto? = null,
    val itemId: Int? = null,

    // Champs éditables (création / édition)
    val itemNumberText: String = "",
    val botanicalvarText: String = "",
    val commonnameText: String = "",
    val cultivarText: String = "",
    val originText: String = "",
    val uomText: String = "",
    val barcodeText: String = "",
    val modelNumText: String = "",
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
    val temperatureMin: Int? = null,
    val temperatureMax: Int? = null,

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
                    itemId = null,
                    apiKey = settings.apiKey,
                    itemNumberText = if (nextItemNumber > 0) nextItemNumber.toString() else "",
                    botanicalvarText = "",
                    quantity = 0.0,
                    commonnameText = "",
                    uomText = "",
                    vendorText = "",
                    wikiText = ""
                )
                return@launch
            }

            runCatching {
                repository.loadItemDetail(itemId)
            }.onSuccess { dto ->
                println("DTO quantity = ${dto.quantity}")
                println("DTO = $dto")
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
                    creationDateText = dto.creationDate.orEmpty()
                )

                val itemNumber = dto.itemNumber?: 0
                if (itemNumber > 0) {
                    loadInventoryByItemNumber(itemNumber)
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
        val itemId = uiState.value.itemId ?: return
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
        val itemId = uiState.value.itemId ?: return
        val pictureUrl = uiState.value.imageUrl ?: return
        val repo = repository ?: return

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

    fun onVendorTextChanged(text: String) {
        uiState.value = uiState.value.copy(vendorText = text)
        refreshVendorOptions(text)
    }

    fun onModelNumChanged(value: String) {
        uiState.update { it.copy(modelNumText = value) }
    }

    private fun refreshVendorOptions(text: String) {
        viewModelScope.launch {
            uiState.value = uiState.value.copy(isVendorLoading = true)

            val rows = repository.loadVendors(
                nbItems = 9999,
                pageNumber = 1
            )

            uiState.value = uiState.value.copy(
                isVendorLoading = false,
                vendorOptions = rows.map { VendorUi(it.description) }
            )
        }
    }

    fun onVendorSelected(vendor: String) {
        uiState.value = uiState.value.copy(vendorText = vendor, vendorOptions = emptyList())
        // plus tard: marquer l’item comme modifié / sauvegarder
    }

    fun onVendorOpen() {
        refreshVendorOptions(uiState.value.vendorText)
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
        itemNumber: Int
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
                                .thenBy { it.binNum ?: "" }
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

    fun onUomChanged(value: String) {
        uiState.update { it.copy(uomText = value) }
    }

    suspend fun save(): Int {
        val currentState = uiState.value

        val itemNumber = currentState.itemNumberText.trim().toIntOrNull()
            ?: throw IllegalArgumentException("No. article invalide")

        val botanicalvar = currentState.botanicalvarText.trim()
        if (botanicalvar.isBlank()) {
            throw IllegalArgumentException("Description requise")
        }

        val vendor       = currentState.vendorText.trim()
        val uom          = currentState.uomText.trim()
        val barcode      = currentState.barcodeText.trim().ifBlank { null }
        val vendorUrl    = currentState.vendorUrlText.trim().ifBlank { null }

        val createdOrUpdatedId = if (currentState.isNewItem) {
            repository.createItem(
                itemNumber = itemNumber,
                botanicalvar = botanicalvar,
                uom = uom,
                vendor = vendor,
                barcode = barcode,
                vendorUrl = vendorUrl
            )
        } else {
            val existingId = currentState.itemId ?: throw IllegalStateException("itemId manquant")
            repository.updateItem(
                itemId = existingId,
                itemNumber = itemNumber,
                botanicalvar = botanicalvar,
                uom = uom,
                vendor = vendor,
                barcode = barcode,
                vendorUrl = vendorUrl
            )
            existingId
        }

        return createdOrUpdatedId
    }

    fun onBarcodeChanged(value: String) {
        uiState.update { it.copy(barcodeText = value) }
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
}
