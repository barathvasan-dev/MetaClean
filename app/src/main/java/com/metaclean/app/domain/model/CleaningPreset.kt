package com.metaclean.app.domain.model

enum class CleaningPreset(
    val displayName: String,
    val description: String,
    val removeGps: Boolean,
    val removeDateTime: Boolean,
    val removeCameraInfo: Boolean,
    val removeSoftware: Boolean,
    val removeAll: Boolean
) {
    SOCIAL_SAFE(
        displayName = "Social Safe",
        description = "Keep camera info, remove GPS and personal data",
        removeGps = true,
        removeDateTime = false,
        removeCameraInfo = false,
        removeSoftware = true,
        removeAll = false
    ),
    ANONYMOUS(
        displayName = "Anonymous",
        description = "Remove all metadata for complete anonymity",
        removeGps = true,
        removeDateTime = true,
        removeCameraInfo = true,
        removeSoftware = true,
        removeAll = true
    ),
    PROFESSIONAL(
        displayName = "Professional Photographer",
        description = "Keep camera/lens info, remove GPS only",
        removeGps = true,
        removeDateTime = false,
        removeCameraInfo = false,
        removeSoftware = false,
        removeAll = false
    ),
    GPS_ONLY(
        displayName = "Remove GPS Only",
        description = "Remove location data, keep everything else",
        removeGps = true,
        removeDateTime = false,
        removeCameraInfo = false,
        removeSoftware = false,
        removeAll = false
    ),
    CUSTOM(
        displayName = "Custom",
        description = "Choose what to remove",
        removeGps = false,
        removeDateTime = false,
        removeCameraInfo = false,
        removeSoftware = false,
        removeAll = false
    );
    
    // Helper to create custom preset with specific settings
    fun withCustomSettings(
        removeGps: Boolean,
        removeDateTime: Boolean,
        removeCameraInfo: Boolean,
        removeSoftware: Boolean
    ): CleaningPreset {
        return when (this) {
            CUSTOM -> CUSTOM // Will use external custom settings
            else -> this
        }
    }
}
