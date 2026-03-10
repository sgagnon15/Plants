package com.sergeapps.plants.data.item

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class UploadPicResponseDto(
    val ok: Boolean,
    val id: Int? = null,
    val url: String? = null,
    val width: Int? = null,
    val height: Int? = null,
    val error: String? = null
)

@Serializable
data class DeletePictureResponseDto(
    val ok: Boolean? = null,
    val result: JsonElement? = null
)
