package com.metaclean.app.domain.model

data class CleaningResult(
    val success: Boolean,
    val originalPath: String,
    val cleanedPath: String? = null,
    val originalSize: Long,
    val cleanedSize: Long? = null,
    val metadataRemoved: Int = 0,
    val fieldsRemoved: List<String> = emptyList(),
    val originalWidth: Int = 0,
    val originalHeight: Int = 0,
    val cleanedWidth: Int = 0,
    val cleanedHeight: Int = 0,
    val errorMessage: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
