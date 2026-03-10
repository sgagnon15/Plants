package com.sergeapps.plants.data.item

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ManufDto(
    @SerialName("pagenumber")
    val pageNumber: String? = null,

    @SerialName("description")
    val description: String? = null
)
