package com.sergeapps.plants.helper

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun formatIsoUtcToLocal(isoUtc: String?): String {
    if (isoUtc.isNullOrBlank()) return "—"

    return try {
        val instant = Instant.parse(isoUtc)
        val localDateTime = instant.atZone(ZoneId.systemDefault()).toLocalDateTime()
        localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
    } catch (e: Exception) {
        isoUtc
    }
}