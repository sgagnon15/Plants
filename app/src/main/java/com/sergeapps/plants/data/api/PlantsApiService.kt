package com.sergeapps.plants.data.api

import com.sergeapps.plants.data.item.DeletePictureResponseDto
import com.sergeapps.plants.data.item.ManufDto
import com.sergeapps.plants.data.item.UploadPicResponseDto
import com.sergeapps.plants.vm.item.UpdateItemDetailDto
import retrofit2.http.GET
import retrofit2.http.Query
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.DELETE
import retrofit2.Response

interface PlantsApiService {
    //  -----------------------------------------------------------------
    // Items
    //  -----------------------------------------------------------------

    @GET("itemlist")
    suspend fun getItemList(
        @Query("pagenumber") pageNumber: Int = 1,
        @Query("orderby") orderBy: String = "botanicalvar",
        @Query("nbitems") nbItems: Int = 15,
        @Query("filter") filter: String? = null
    ): List<ItemListDto>

    @GET("itemdetail")
    suspend fun getItemDetail(
        @Query("id") id: Int
    ): ItemDetailDto

    @GET("getCare")
    suspend fun getPlantCare(
        @Query("plantname") plantName: String
    ): PlantCareDto

    @POST("item")
    suspend fun updateItemDetail(
        @Query("id") itemId: Int,
        @Body item: UpdateItemDetailDto
    ): Response<Unit>

    //  -----------------------------------------------------------------
    // Vendors
    //  -----------------------------------------------------------------

    @GET("vendorlist")
    suspend fun getVendorList(
        @Query("pagenumber") pageNumber: Int = 1,
        @Query("nbitems") nbItems: Int = 9999
    ): List<VendorDto>

    @GET("manufList")
    suspend fun getManufList(
        @Query("nbitems") nbItems: Int,
        @Query("pagenumber") pageNumber: Int
    ): List<ManufDto>

    @GET("nextitem")
    suspend fun getNextItem(): NextItemDto

    @POST("item")
    suspend fun upsertItem(
        @Query("id") itemId: Int,
        @Body body: ItemUpsertDto
    ): retrofit2.Response<Unit>

    //  -----------------------------------------------------------------
    // Pictures
    //  -----------------------------------------------------------------

    @Multipart
    @POST("uploadpic")
    suspend fun uploadPic(
        @Query("id") id: Int,
        @Part file: MultipartBody.Part
    ): UploadPicResponseDto

    @DELETE("picture")
    suspend fun deletePicture(
        @Query("id") id: Int,
        @Query("url") url: String
    ): DeletePictureResponseDto

    //  -----------------------------------------------------------------
    // Inventory
    //  -----------------------------------------------------------------

    @GET("nextSpecimen")
    suspend fun getNextSpecimen(): NextSpecimenDto

    @GET("location")
    suspend fun getLocations(): List<LocationDto>

    @GET("locposition")
    suspend fun getLocationPositions(
        @Query("location") location: String
    ): List<PositionDto>

    @GET("whereused")
    suspend fun getWhereUsed(
        @Query("tablename") tableName: String,
        @Query("attrname") attrName: String,
        @Query("value") value: String
    ): WhereUsedDto

    @GET("stocklist")
    suspend fun getStockList(
        @Query("nbitems") nbItems: Int = 15,
        @Query("orderby") orderBy: String = "position",
        @Query("pagenumber") pageNumber: Int = 1,
        @Query("location") location: String
    ): List<StockListRowDto>

    @GET("stocklist")
    suspend fun getStockList(
        @Query("itemnumber") itemNumber: String,
        @Query("pagenumber") pageNumber: Int = 1,
        @Query("nbitems") nbItems: Int = 200
    ): List<StockListDto>

    @GET("stockdetail")
    suspend fun getStockDetail(
        @Query("id") stockId: Int,
        @Query("itemnumber") itemNumber: String? = null
    ): StockDetailDto

    @POST("location")
    suspend fun updateLocation(
        @Query("id") id: Int,
        @Body body: UpdateLocationBody
    )

    @POST("location")
    suspend fun createLocation(
        @Query("id") id: Int,
        @Body body: CreateLocationBody
    )

    @POST("stock")
    suspend fun upsertStock(
        @Query("id") stockId: Int,
        @Body body: StockUpsertRequest
    ): StockCreateResponseDto

    @POST("stock")
    suspend fun moveStockLocation(
        @Query("id") stockId: Int,
        @Body body: StockMoveLocationBody
    ): StockCreateResponseDto

    @DELETE("stock")
    suspend fun deleteStock(
        @Query("id") stockId: Int
    )

    @DELETE("location")
    suspend fun deleteLocation(
        @Query("id") id: Int
    )

    //  -----------------------------------------------------------------
    // Controller
    //  -----------------------------------------------------------------

    @GET("info")
    suspend fun getInfo(
        @Query("macaddress") macAddress: String
    ): ControlInfoDto

    @GET("chrono")
    suspend fun getChrono(
        @Query("macaddress") macAddress: String
    ): ChronoDto

    @GET("genparam")
    suspend fun getGenParam(
        @Query("controlername") controlername: String
    ): GenParamDto

    @GET("controlerlist")
    suspend fun getControllerList(
    ): List<ControllerListDto>

    @GET("log")
    suspend fun getLog(
        @Query("limit") limit: Int,
        @Query("controlername") controlername: String
    ): List<LogListDto>

    @GET("schedulelist")
    suspend fun getScheduleList(
        @Query("macaddress") macAddress: String,
        @Query("zone") zone: String
    ): List<ScheduleListDto>

    @POST("water")
    suspend fun setWater(
        @Query("state") state: String,
        @Query("duration") duration: Int,
        @Query("macaddress") macAddress: String
    )
}
