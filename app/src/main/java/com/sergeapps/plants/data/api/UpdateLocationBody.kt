package com.sergeapps.plants.data.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UpdateLocationBody(
    @SerialName("location") val location: String,
    @SerialName("nbbin") val nbBin: Int,
    @SerialName("type") val type: String
)