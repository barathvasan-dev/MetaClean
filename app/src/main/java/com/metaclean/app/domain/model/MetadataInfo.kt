package com.metaclean.app.domain.model

data class MetadataInfo(
    val filePath: String,
    val fileName: String,
    val fileSize: Long,
    val mimeType: String,
    val exifData: Map<String, String> = emptyMap(),
    val iptcData: Map<String, String> = emptyMap(),
    val xmpData: Map<String, String> = emptyMap(),
    val hasGps: Boolean = false,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val dateTimeTaken: String? = null,
    val cameraModel: String? = null,
    val cameraManufacturer: String? = null,
    val lens: String? = null,
    val iso: String? = null,
    val shutterSpeed: String? = null,
    val aperture: String? = null,
    val focalLength: String? = null,
    val software: String? = null
)
