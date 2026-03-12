package com.sergeapps.plants.data.api
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

//  -----------------------------------------------------------------
// Items
//  -----------------------------------------------------------------

@Serializable
data class ItemUpsertDto(
    @SerialName("itemnumber")   val itemNumber: String,
    @SerialName("botanicalVar") val botanicalVar: String,
    @SerialName("commonName")   val commonName: String? = null,
    @SerialName("cultivar")     val cultivar: String? = null,
    @SerialName("origin")       val origin: String? = null,
    @SerialName("wiki")         val wiki: String? = null,
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
    @SerialName("temperature")  val temperature: String? = null,
    @SerialName("AIUpdated")    val AIUpdated: Int = 0
)

@Serializable
data class NextItemDto(
    @SerialName("nextItem") val nextItem: Int
)

@Serializable
data class ItemDetailDto(
    @SerialName("id") val id: Int,
    @SerialName("itemnumber")   val itemNumber: String,
    @SerialName("botanicalVar") val botanicalVar: String,
    @SerialName("commonName")   val commonName: String,
    @SerialName("quantity")     val quantity: Int = 0,
    @SerialName("cultivar")     val cultivar: String,
    @SerialName("origin")       val origin: String,
    @SerialName("vendor")       val vendor: String? = null,
    @SerialName("creationDate") val creationDate: String? = null,
    @SerialName("vendorUrl")    val vendorUrl: String? = null,
    @SerialName("url")          val url: String? = null,
    @SerialName("wiki")         val wiki: String? = null,
    @SerialName("thumbnailurl") val thumbnailurl: String? = null,
    @SerialName("AIUpdated")    val AIUpdated: Int = 0,
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
    @SerialName("itemnumber")   val itemNumber: String,
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
data class NextSpecimenDto(
    @SerialName("nextSpecimen") val nextSpecimen: Int
)

@Serializable
data class StockListRowDto(
    @SerialName("pagenumber")       val pagenumber: Int,
    @SerialName("itemnumber")       val itemNumber: String,
    @SerialName("specimennumber")   val specimenNumber: Int? = null,
    @SerialName("picturerotation")  val picturerotation: Int,
    @SerialName("location")         val location: String,
    @SerialName("position")         val position: String,
    @SerialName("id")               val stockId: Int,
    @SerialName("commonName")       val commonName: String,
    @SerialName("botanicalvar")     val botanicalvar: String,
    @SerialName("cultivar")         val cultivar: String? = null,
    @SerialName("thumbnailurl")     val thumbnailurl: String? = null,
    @SerialName("photoversion")     val photoVersion: Int? = null
)

@Serializable
data class StockDetailDto(
    @SerialName("id")               val stockId: Int,
    @SerialName("specimenNumber")   val specimenNumber: String? = null,
    @SerialName("itemnumber")       val itemNumber: String,
    @SerialName("creationDate")     val creationDate: String,
    @SerialName("location")         val location: String,
    @SerialName("position")         val position: String,
    @SerialName("purchaseDate")     val purchaseDate: String?,
    @SerialName("lastTransplant")   val lastTransplant: String?,
    @SerialName("lastDivision")     val lastDivision: String?,
    @SerialName("lastFeeding")      val lastFeeding: String?,
    @SerialName("purchasePrice")    val purchasePrice: Double?,
    @SerialName("fromSpecimen")     val fromSpecimen: String?,
    @SerialName("vendor")           val vendor: String?,
    @SerialName("url")              val url: String?,
    @SerialName("botanicalvar")     val botanicalvar: String,
    @SerialName("thumbnailurl")     val thumbnailurl: String? = null,
    @SerialName("photoversion")     val photoVersion: Int? = null
)

@Serializable
data class PositionDto(
    @SerialName("position")
    val position: String
)

@Serializable
data class StockUpsertRequest(
    @SerialName("itemnumber")     val itemNumber: String,
    @SerialName("specimennumber") val specimenNumber: String? = null,
    @SerialName("vendor")         val vendor: String? = null,
    @SerialName("location")       val location: String,
    @SerialName("position")       val position: String,
    @SerialName("purchaseDate")   val purchaseDate: String? = null,
    @SerialName("purchasePrice")  val purchasePrice: String? = null,
    @SerialName("lastTransplant") val lastTransplant: String? = null,
    @SerialName("lastDivision")   val lastDivision: String? = null,
    @SerialName("lastFeeding")    val lastFeeding: String? = null
)

@Serializable
data class StockListDto(
    @SerialName("id") val id: Int,
    @SerialName("location") val location: String? = null,
    @SerialName("position") val position: String? = null,
    @SerialName("quantity") val quantity: String? = null
)

@Serializable
data class StockCreateResponseDto(
    @SerialName("stockid")    val stockId: Int,
    @SerialName("itemnumber") val itemNumber: String
)

@Serializable
data class LocationDto(
    @SerialName("id") val id: Int,
    @SerialName("location") val location: String,
    @SerialName("nbbin") val nbBin: Int? = null,
    @SerialName("type") val type: String? = null
)

@Serializable
data class StockMoveLocationBody(
    @SerialName("location") val location: String,
    @SerialName("position") val position: String
)

@Serializable
data class CreateLocationBody(
    @SerialName("location") val location: String,
    @SerialName("nbbin") val nbBin: Int,
    @SerialName("type") val type: String? = null
)

@Serializable
data class VendorDto(
    @SerialName("vendor")
    val vendor: String
)

@Serializable
data class WhereUsedDto(
    @SerialName("occurence") val occurrence: Int
)
