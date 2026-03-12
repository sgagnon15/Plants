package com.sergeapps.plants.data

import android.content.Context
import android.net.Uri
import com.sergeapps.plants.data.api.CreateLocationBody
import com.sergeapps.plants.data.api.ItemDetailDto
import com.sergeapps.plants.data.api.ItemListDto
import com.sergeapps.plants.data.api.ItemUpsertDto
import com.sergeapps.plants.data.api.LocationDto
import com.sergeapps.plants.data.api.PlantsApiService
import com.sergeapps.plants.data.api.StockDetailDto
import com.sergeapps.plants.data.api.StockListRowDto
import com.sergeapps.plants.data.api.StockUpsertRequest
import com.sergeapps.plants.data.api.UpdateLocationBody
import com.sergeapps.plants.data.item.DeletePictureResponseDto
import com.sergeapps.plants.data.item.UploadPicResponseDto
import com.sergeapps.plants.ui.item.InventoryRowUi
import com.sergeapps.plants.util.MultipartUtils
import com.sergeapps.plants.data.api.PlantCareDto
import com.sergeapps.plants.vm.item.UpdateItemDetailDto


class PlantsRepository(
    private val api: PlantsApiService
) {
    // --- ITEMS ---

    suspend fun createItem(
        itemNumber: String,
        botanicalvar: String,
        commonName: String? = null,
        cultivar: String? = null,
        origin: String? = null,
        wiki: String? = null,
        light: String? = null,
        soil: String? = null,
        water: String? = null,
        temperature: String? = null,
        dormancy: String? = null,
        feed: String? = null,
        aiUpdated: Int = 0
    ): String {
        api.upsertItem(
            itemId = 0,
            body = ItemUpsertDto(
                itemNumber = itemNumber,
                botanicalVar = botanicalvar,
                commonName = commonName,
                cultivar = cultivar,
                origin = origin,
                wiki = wiki,
                light = light,
                soil = soil,
                water = water,
                temperature = temperature,
                dormancy = dormancy,
                feed = feed,
                pictureRotation = 0,
                AIUpdated = aiUpdated
            )
        )
        return itemNumber
    }

    suspend fun updateItem(
        itemId: Int,
        itemNumber: String,
        botanicalvar: String,
        commonName: String? = null,
        cultivar: String? = null,
        origin: String? = null,
        wiki: String? = null,
        light: String? = null,
        soil: String? = null,
        water: String? = null,
        temperature: String? = null,
        dormancy: String? = null,
        feed: String? = null,
        aiUpdated: Int = 0
    ) {
        api.upsertItem(
            itemId = itemId,
            body = ItemUpsertDto(
                itemNumber = itemNumber,
                botanicalVar = botanicalvar,
                commonName = commonName,
                cultivar = cultivar,
                origin = origin,
                wiki = wiki,
                light = light,
                soil = soil,
                water = water,
                temperature = temperature,
                dormancy = dormancy,
                feed = feed,
                pictureRotation = 0,
                AIUpdated = aiUpdated
            )
        )
    }

    suspend fun getNextItemNumber(): Int {
        return api.getNextItem().nextItem
    }

    suspend fun loadItemsPage(
        page: Int,
        nbItems: Int,
        orderBy: String = "botanicalvar",
        filter: String? = null
    ): List<ItemListDto> {
        return api.getItemList(
            pageNumber = page,
            orderBy = "botanicalvar",
            nbItems = nbItems,
            filter  = filter?.takeIf { it.isNotBlank() }
        )
    }

    suspend fun loadItemDetail(itemId: Int): ItemDetailDto {
        return api.getItemDetail(id = itemId)
    }

    // --- PHOTO ---

    suspend fun uploadPhoto(
        context: Context,
        itemId: Int,
        uri: Uri
    ): UploadPicResponseDto {
        val part = MultipartUtils.uriToMultipart(
            context = context,
            uri = uri,
            partName = "file" // le nom n'est pas important pour ton busboy, mais c'est plus clair
        )

        return api.uploadPic(
            id = itemId,
            file = part
        )
    }

    suspend fun deletePhoto(itemId: Int, pictureUrl: String): DeletePictureResponseDto {
        return api.deletePicture(
            id = itemId,
            url = pictureUrl
        )
    }

    // --- VENDORS & MANUFACTURERS ---

    suspend fun loadVendors(): List<String> {
        return api.getVendorList(
            pageNumber = 1,
            nbItems = 9999
        )
            .map { it.vendor.trim() }
            .filter { it.isNotBlank() }
            .distinct()
            .sorted()
    }

    // --- LOCATIONS ---

    suspend fun loadLocations(): List<LocationDto> {
        return api.getLocations()
            .sortedBy { dto -> dto.location.uppercase() }
    }

    suspend fun getLocations(): List<LocationDto> {
        return api.getLocations()
    }

    suspend fun createLocation(location: String, nbBin: Int) {
        val body = CreateLocationBody(location = location, nbBin = nbBin)
        return api.createLocation(locationQuery = location, body = body)
    }

    suspend fun updateLocation(id: Int, location: String, nbBin: Int, type: String) {
        val body = UpdateLocationBody(
            location = location,
            nbBin = nbBin,
            type = type
        )
        return api.updateLocation(id = id, body = body)
    }

    suspend fun getLocationUsageCount(locationValue: String): Int {
        val dto = api.getWhereUsed(
            tableName = "stock",
            attrName = "location",
            value = locationValue
        )
        return dto.occurrence
    }

    suspend fun deleteLocation(id: Int) {
        api.deleteLocation(id)
    }

    // --- INVENTORY ---

    suspend fun loadStockListPage(
        location: String,
        page: Int,
        nbItems: Int,
        orderBy: String = "Position"
    ): List<StockListRowDto> {
        return api.getStockList(
            nbItems = nbItems,
            orderBy = orderBy,
            pageNumber = page,
            location = location
        )
    }

    suspend fun loadStockDetail(
        stockId: Int,
        itemNumber: String? = null
    ): StockDetailDto {
        return api.getStockDetail(stockId, itemNumber)
    }

    suspend fun upsertStock(
        stockId: Int,
        body: StockUpsertRequest
    ): Int {
        val response = api.upsertStock(
            stockId = stockId,
            body = body
        )

        return response.stockId
    }

    suspend fun fetchInventoryByItemNumber(
        itemNumber: String
    ): List<InventoryRowUi> {

        val response = api.getStockList(
            itemNumber = itemNumber,
            pageNumber = 1,
            nbItems = 200
        )

        return response.map { dto ->

            InventoryRowUi(
                stockId = dto.id,
                location = dto.location?.trim().orEmpty(),
                position = dto.position?.trim(),
                quantity = dto.quantity?.toDouble()?: 0.0
            )
        }
    }

    suspend fun deleteStock(stockId: Int) {
        api.deleteStock(stockId)
    }

    suspend fun getPlantCare(plantName: String): PlantCareDto {
        try {
            val result = api.getPlantCare(plantName)
            return result

        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun updateItemDetail(item: UpdateItemDetailDto): Result<Unit> {
        return try {
            val response = api.updateItemDetail(item.id, item)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Erreur HTTP ${response.code()}"))
            }
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    suspend fun getNextSpecimen(): Int {
        return api.getNextSpecimen().nextSpecimen
    }

    suspend fun loadPositions(location: String): List<String> {
        return api.getLocationPositions(location)
            .map { it.position.trim() }
            .filter { it.isNotBlank() }
            .distinct()
            .sorted()
    }
}

