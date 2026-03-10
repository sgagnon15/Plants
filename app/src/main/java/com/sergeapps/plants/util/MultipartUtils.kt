package com.sergeapps.plants.util

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

object MultipartUtils {

    fun uriToMultipart(
        context: Context,
        uri: Uri,
        partName: String
    ): MultipartBody.Part {
        val contentResolver = context.contentResolver

        val mimeType = contentResolver.getType(uri) ?: "image/jpeg"
        val mediaType = mimeType.toMediaTypeOrNull()

        val inputStream = contentResolver.openInputStream(uri)
            ?: error("Impossible d'ouvrir le Uri")

        val bytes = inputStream.use { it.readBytes() }

        val fileName = guessFileName(contentResolver, uri) ?: "photo.jpg"

        val requestBody = bytes.toRequestBody(mediaType)

        return MultipartBody.Part.createFormData(
            name = partName,
            filename = fileName,
            body = requestBody
        )
    }

    private fun guessFileName(
        contentResolver: ContentResolver,
        uri: Uri
    ): String? {
        val cursor = contentResolver.query(uri, null, null, null, null) ?: return null
        return cursor.use {
            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex >= 0 && it.moveToFirst()) it.getString(nameIndex) else null
        }
    }
}
