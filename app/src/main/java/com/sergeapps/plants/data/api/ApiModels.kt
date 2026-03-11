package com.sergeapps.plants.data.api
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

//  -----------------------------------------------------------------
// Items
//  -----------------------------------------------------------------

@Serializable
data class ItemUpsertDto(
    @SerialName("itemnumber") val itemNumber: Int,
    @SerialName("botanicalvar") val botanicalVar: String,
    @SerialName("uom") val uom: String,
    @SerialName("vendor") val vendor: String,
    @SerialName("barcode") val barcode: String? = null,
    @SerialName("vendorurl") val vendorUrl: String? = null
)

@Serializable
data class NextItemDto(
    @SerialName("nextItem") val nextItem: Int
)

@Serializable
data class ItemDetailDto(
    @SerialName("id") val id: Int,
    @SerialName("itemnumber")   val itemNumber: Int,
    @SerialName("botanicalVar") val botanicalVar: String,
    @SerialName("commonName")   val commonName: String,
    @SerialName("quantity")     val quantity: Int = 0,
    @SerialName("cultivar")     val cultivar: String,
    @SerialName("origin")       val origin: String,
    @SerialName("barcode")      val barcode: String? = null,
    @SerialName("manufacturer") val manufacturer: String? = null,
    @SerialName("vendor")       val vendor: String? = null,
    @SerialName("creationDate") val creationDate: String? = null,
    @SerialName("vendorUrl")    val vendorUrl: String? = null,
    @SerialName("avgcost")      val avgCost: String? = null,
    @SerialName("url")          val url: String? = null,
    @SerialName("wiki")         val wiki: String? = null,
    @SerialName("thumbnailurl") val thumbnailurl: String? = null,
    @SerialName("pictureRotation") val pictureRotation: Int,

    //  Care instructions
    @SerialName("light")        val light: String? = null,
    @SerialName("soil")         val soil: String? = null,
    @SerialName("water")        val water: String? = null,
    @SerialName("feed")         val feed: String? = null,
    @SerialName("dormancy")     val dormancy: String? = null,
    @SerialName("transplant")   val transplant: String? = null,
    @SerialName("otherCare")    val otherCare: String? = null,
    @SerialName("humidity")     val humidity: String? = null,
    @SerialName("temperature")  val temperature: String? = null
)

@Serializable
data class PlantCareDto(
    @SerialName("arrosage") val arrosage: String? = null,
    @SerialName("lumière") val lumière: String? = null,
    @SerialName("température") val température: String? = null,
    @SerialName("engrais") val engrais: String? = null,
    @SerialName("substrat") val substrat: String? = null,
    @SerialName("dormance") val dormance: String? = null,
    @SerialName("rempotage") val rempotage: String? = null,
    @SerialName("humidité") val humidité: String? = null
)

@Serializable
data class ItemListDto(
    @SerialName("id")           val id: Int,
    @SerialName("itemnumber")   val itemNumber: Int,
    @SerialName("botanicalVar") val botanicalVar: String,
    @SerialName("commonName")   val commonName: String,
    @SerialName("quantity")     val quantity: Double,
    @SerialName("origin")       val origin: String,
    @SerialName("vendor")       val vendor: String? = null,
    @SerialName("creationdate") val creationDate: String? = null,
    @SerialName("vendorUrl")    val vendorUrl: String? = null,
    @SerialName("avgcost")      val avgCost: String? = null,
    @SerialName("url")          val url: String? = null,
    @SerialName("pagenumber")   val pageNumber: String? = null,
    @SerialName("thumbnailurl") val thumbnailUrl: String?,
    @SerialName("pictureRotation")   val pictureRotation: Int
)

//  -----------------------------------------------------------------
// Inventory & Locations
//  -----------------------------------------------------------------

@Serializable
data class StockListRowDto(
    @SerialName("pagenumber") val pageNumber: String? = null,
    @SerialName("itemnumber") val itemNumber: String,
    @SerialName("location") val location: String,
    @SerialName("binnum") val binNum: String,
    @SerialName("quantity") val quantity: String,
    @SerialName("id") val stockId: Int,
    @SerialName("botanicalvar") val botanicalvar: String,
    @SerialName("thumbnailurl") val thumbnailurl: String? = null,
    @SerialName("photoversion") val photoVersion: Int? = null
)

@Serializable
data class StockDetailDto(
    @SerialName("location") val location: String,
    @SerialName("binnum") val binNum: String,
    @SerialName("quantity") val quantity: String,
    @SerialName("stockId") val stockId: Int,

    @SerialName("id") val itemId: Int,
    @SerialName("itemnumber") val itemNumber: Int,
    @SerialName("botanicalvar") val botanicalvar: String,
    @SerialName("uom") val uom: String? = null,
    @SerialName("barcode") val barcode: String? = null,
    @SerialName("manufacturer") val manufacturer: String? = null,
    @SerialName("vendor") val vendor: String? = null,
    @SerialName("minlevel") val minLevel: String? = null,
    @SerialName("maxlevel") val maxLevel: String? = null,
    @SerialName("creationdate") val creationDate: String? = null,
    @SerialName("sku") val sku: String? = null,
    @SerialName("vendorUrl") val vendorUrl: String? = null,
    @SerialName("classid") val classId: Int? = null,
    @SerialName("modelnum") val modelNum: String? = null,
    @SerialName("avgcost") val avgCost: String? = null,
    @SerialName("classdesc") val classDesc: String? = null,
    @SerialName("url") val url: String? = null,
    @SerialName("photoversion") val photoVersion: Int? = null
)

@Serializable
data class StockUpsertRequest(
    @SerialName("itemnumber") val itemNumber: Int,
    @SerialName("location") val location: String,
    @SerialName("binnum") val binNum: String,
    @SerialName("quantity") val quantity: Int
)

@Serializable
data class StockTransDto(
    @SerialName("projectid") val projectId: String,
    @SerialName("usage") val usage: String,
    @SerialName("transtype") val transType: String, // "ISSUE" ou "RETURN"
    @SerialName("itemnumber") val itemNumber: String,
    @SerialName("location") val location: String,
    @SerialName("binnum") val binNum: String,
    @SerialName("transqty") val transQty: Int,
    @SerialName("uomcode") val uomCode: String
)

@Serializable
data class StockListDto(
    @SerialName("id") val id: Int,
    @SerialName("location") val location: String? = null,
    @SerialName("binnum") val binNum: String? = null,
    @SerialName("quantity") val quantity: String? = null
)

@Serializable
data class StockCreateResponseDto(
    @SerialName("stockid") val stockId: Int,
    @SerialName("itemnumber") val itemNumber: Int
)

@Serializable
data class VendorRowDto(
    val pagenumber: String,
    val description: String
)

@Serializable
data class LocationDto(
    @SerialName("id") val id: Int,
    @SerialName("location") val location: String,
    @SerialName("nbbin") val nbBin: Int? = null,
    @SerialName("type") val type: String? = null
)

@Serializable
data class CreateLocationBody(
    @SerialName("location") val location: String,
    @SerialName("nbbin") val nbBin: Int
)

//  -----------------------------------------------------------------
// Classifications & Specifications
//  -----------------------------------------------------------------

@Serializable
data class WhereUsedDto(
    @SerialName("occurence") val occurrence: Int
)
