package com.metaclean.app.domain.metadata

import android.content.Context
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import com.metaclean.app.domain.model.CleaningPreset
import com.metaclean.app.domain.model.CleaningResult
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

class MetadataCleaner(private val context: Context) {
    
    suspend fun cleanMetadata(
        uri: Uri,
        preset: CleaningPreset,
        outputFile: File,
        overwriteOriginal: Boolean = false
    ): CleaningResult {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return CleaningResult(
                    success = false,
                    originalPath = uri.toString(),
                    originalSize = 0,
                    errorMessage = "Cannot open file"
                )
            
            val originalSize = inputStream.available().toLong()
            
            // Create temporary file
            val tempFile = File.createTempFile("metaclean_", ".tmp", context.cacheDir)
            
            // Copy original to temp
            tempFile.outputStream().use { output ->
                inputStream.copyTo(output)
            }
            inputStream.close()
            
            // Remove metadata based on preset
            val exif = ExifInterface(tempFile.absolutePath)
            val originalWidth = exif.getAttributeInt(ExifInterface.TAG_IMAGE_WIDTH, 0)
            val originalHeight = exif.getAttributeInt(ExifInterface.TAG_IMAGE_LENGTH, 0)
            
            val removalResult = removeMetadataByPreset(exif, preset)
            exif.saveAttributes()
            
            // Copy to output location
            if (overwriteOriginal) {
                // Note: Overwriting original requires special handling with content resolver
                // This is simplified for demonstration
                context.contentResolver.openOutputStream(uri)?.use { output ->
                    tempFile.inputStream().copyTo(output)
                }
            } else {
                tempFile.copyTo(outputFile, overwrite = true)
            }
            
            val cleanedSize = outputFile.length()
            val cleanedExif = ExifInterface(outputFile.absolutePath)
            val cleanedWidth = cleanedExif.getAttributeInt(ExifInterface.TAG_IMAGE_WIDTH, originalWidth)
            val cleanedHeight = cleanedExif.getAttributeInt(ExifInterface.TAG_IMAGE_LENGTH, originalHeight)
            tempFile.delete()
            
            CleaningResult(
                success = true,
                originalPath = uri.toString(),
                cleanedPath = outputFile.absolutePath,
                originalSize = originalSize,
                cleanedSize = cleanedSize,
                metadataRemoved = removalResult.count,
                fieldsRemoved = removalResult.fields,
                originalWidth = originalWidth,
                originalHeight = originalHeight,
                cleanedWidth = cleanedWidth,
                cleanedHeight = cleanedHeight
            )
        } catch (e: Exception) {
            e.printStackTrace()
            CleaningResult(
                success = false,
                originalPath = uri.toString(),
                originalSize = 0,
                errorMessage = e.message
            )
        }
    }
    
    suspend fun cleanMetadataFromFile(
        inputFile: File,
        preset: CleaningPreset,
        outputFile: File,
        customRemoveGps: Boolean? = null,
        customRemoveDateTime: Boolean? = null,
        customRemoveCameraInfo: Boolean? = null,
        customRemoveSoftware: Boolean? = null
    ): CleaningResult {
        val originalSize = inputFile.length()
        return try {
            // Get original dimensions
            val originalExif = ExifInterface(inputFile.absolutePath)
            val originalWidth = originalExif.getAttributeInt(ExifInterface.TAG_IMAGE_WIDTH, 0)
            val originalHeight = originalExif.getAttributeInt(ExifInterface.TAG_IMAGE_LENGTH, 0)
            
            // Copy file
            inputFile.copyTo(outputFile, overwrite = true)
            
            // Remove metadata
            val exif = ExifInterface(outputFile.absolutePath)
            val removalResult = removeMetadataByPreset(
                exif, 
                preset, 
                customRemoveGps, 
                customRemoveDateTime, 
                customRemoveCameraInfo, 
                customRemoveSoftware
            )
            exif.saveAttributes()
            
            val cleanedSize = outputFile.length()
            val cleanedExif = ExifInterface(outputFile.absolutePath)
            val cleanedWidth = cleanedExif.getAttributeInt(ExifInterface.TAG_IMAGE_WIDTH, originalWidth)
            val cleanedHeight = cleanedExif.getAttributeInt(ExifInterface.TAG_IMAGE_LENGTH, originalHeight)
            
            CleaningResult(
                success = true,
                originalPath = inputFile.absolutePath,
                cleanedPath = outputFile.absolutePath,
                originalSize = originalSize,
                cleanedSize = cleanedSize,
                metadataRemoved = removalResult.count,
                fieldsRemoved = removalResult.fields,
                originalWidth = originalWidth,
                originalHeight = originalHeight,
                cleanedWidth = cleanedWidth,
                cleanedHeight = cleanedHeight
            )
        } catch (e: Exception) {
            e.printStackTrace()
            CleaningResult(
                success = false,
                originalPath = inputFile.absolutePath,
                originalSize = originalSize,
                errorMessage = e.message
            )
        }
    }
    
    private data class RemovalResult(val count: Int, val fields: List<String>)
    
    private fun removeMetadataByPreset(
        exif: ExifInterface, 
        preset: CleaningPreset,
        customRemoveGps: Boolean? = null,
        customRemoveDateTime: Boolean? = null,
        customRemoveCameraInfo: Boolean? = null,
        customRemoveSoftware: Boolean? = null
    ): RemovalResult {
        val removedFields = mutableListOf<String>()
        
        // Use custom settings if CUSTOM preset and custom values provided
        val shouldRemoveGps = if (preset == CleaningPreset.CUSTOM && customRemoveGps != null) customRemoveGps else preset.removeGps
        val shouldRemoveDateTime = if (preset == CleaningPreset.CUSTOM && customRemoveDateTime != null) customRemoveDateTime else preset.removeDateTime
        val shouldRemoveCameraInfo = if (preset == CleaningPreset.CUSTOM && customRemoveCameraInfo != null) customRemoveCameraInfo else preset.removeCameraInfo
        val shouldRemoveSoftware = if (preset == CleaningPreset.CUSTOM && customRemoveSoftware != null) customRemoveSoftware else preset.removeSoftware
        
        if (preset.removeAll) {
            val result = removeAllMetadata(exif)
            return RemovalResult(result.count, result.fields)
        } else {
            if (shouldRemoveGps) {
                val result = removeGpsData(exif)
                removedFields.addAll(result.fields)
            }
            if (shouldRemoveDateTime) {
                val result = removeDateTimeData(exif)
                removedFields.addAll(result.fields)
            }
            if (shouldRemoveCameraInfo) {
                val result = removeCameraData(exif)
                removedFields.addAll(result.fields)
            }
            if (shouldRemoveSoftware) {
                val result = removeSoftwareData(exif)
                removedFields.addAll(result.fields)
            }
        }
        
        return RemovalResult(removedFields.size, removedFields)
    }
    
    private fun removeAllMetadata(exif: ExifInterface): RemovalResult {
        // Comprehensive list of ALL EXIF tags for complete metadata removal
        val tags = listOf(
            // Image Orientation & Structure
            "Orientation" to ExifInterface.TAG_ORIENTATION,
            "Image Width" to ExifInterface.TAG_IMAGE_WIDTH,
            "Image Length" to ExifInterface.TAG_IMAGE_LENGTH,
            "Bits Per Sample" to ExifInterface.TAG_BITS_PER_SAMPLE,
            "Compression" to ExifInterface.TAG_COMPRESSION,
            "Photometric Interpretation" to ExifInterface.TAG_PHOTOMETRIC_INTERPRETATION,
            "Samples Per Pixel" to ExifInterface.TAG_SAMPLES_PER_PIXEL,
            "Planar Configuration" to ExifInterface.TAG_PLANAR_CONFIGURATION,
            "YCbCr Sub Sampling" to ExifInterface.TAG_Y_CB_CR_SUB_SAMPLING,
            "YCbCr Positioning" to ExifInterface.TAG_Y_CB_CR_POSITIONING,
            "YCbCr Coefficients" to ExifInterface.TAG_Y_CB_CR_COEFFICIENTS,
            
            // Resolution & Units
            "X Resolution" to ExifInterface.TAG_X_RESOLUTION,
            "Y Resolution" to ExifInterface.TAG_Y_RESOLUTION,
            "Resolution Unit" to ExifInterface.TAG_RESOLUTION_UNIT,
            
            // Date & Time
            "DateTime" to ExifInterface.TAG_DATETIME,
            "DateTimeDigitized" to ExifInterface.TAG_DATETIME_DIGITIZED,
            "DateTimeOriginal" to ExifInterface.TAG_DATETIME_ORIGINAL,
            "SubSecTime" to ExifInterface.TAG_SUBSEC_TIME,
            "SubSecTimeOriginal" to ExifInterface.TAG_SUBSEC_TIME_ORIGINAL,
            "SubSecTimeDigitized" to ExifInterface.TAG_SUBSEC_TIME_DIGITIZED,
            "OffsetTime" to ExifInterface.TAG_OFFSET_TIME,
            "OffsetTimeOriginal" to ExifInterface.TAG_OFFSET_TIME_ORIGINAL,
            "OffsetTimeDigitized" to ExifInterface.TAG_OFFSET_TIME_DIGITIZED,
            
            // Camera & Device
            "Make" to ExifInterface.TAG_MAKE,
            "Model" to ExifInterface.TAG_MODEL,
            "Software" to ExifInterface.TAG_SOFTWARE,
            "BodySerialNumber" to ExifInterface.TAG_BODY_SERIAL_NUMBER,
            "CameraOwnerName" to ExifInterface.TAG_CAMERA_OWNER_NAME,
            
            // GPS Location Data (ALL GPS tags)
            "GPS Latitude" to ExifInterface.TAG_GPS_LATITUDE,
            "GPS Longitude" to ExifInterface.TAG_GPS_LONGITUDE,
            "GPS Altitude" to ExifInterface.TAG_GPS_ALTITUDE,
            "GPS Timestamp" to ExifInterface.TAG_GPS_TIMESTAMP,
            "GPS Date" to ExifInterface.TAG_GPS_DATESTAMP,
            "GPS Processing Method" to ExifInterface.TAG_GPS_PROCESSING_METHOD,
            "GPS Speed" to ExifInterface.TAG_GPS_SPEED,
            "GPS SpeedRef" to ExifInterface.TAG_GPS_SPEED_REF,
            "GPS Track" to ExifInterface.TAG_GPS_TRACK,
            "GPS TrackRef" to ExifInterface.TAG_GPS_TRACK_REF,
            "GPS ImgDirection" to ExifInterface.TAG_GPS_IMG_DIRECTION,
            "GPS ImgDirectionRef" to ExifInterface.TAG_GPS_IMG_DIRECTION_REF,
            "GPS DestBearing" to ExifInterface.TAG_GPS_DEST_BEARING,
            "GPS DestBearingRef" to ExifInterface.TAG_GPS_DEST_BEARING_REF,
            "GPS DestDistance" to ExifInterface.TAG_GPS_DEST_DISTANCE,
            "GPS DestDistanceRef" to ExifInterface.TAG_GPS_DEST_DISTANCE_REF,
            "GPS DestLatitude" to ExifInterface.TAG_GPS_DEST_LATITUDE,
            "GPS DestLongitude" to ExifInterface.TAG_GPS_DEST_LONGITUDE,
            "GPS MapDatum" to ExifInterface.TAG_GPS_MAP_DATUM,
            "GPS Satellites" to ExifInterface.TAG_GPS_SATELLITES,
            "GPS Status" to ExifInterface.TAG_GPS_STATUS,
            "GPS MeasureMode" to ExifInterface.TAG_GPS_MEASURE_MODE,
            "GPS DOP" to ExifInterface.TAG_GPS_DOP,
            "GPS VersionID" to ExifInterface.TAG_GPS_VERSION_ID,
            "GPS AltitudeRef" to ExifInterface.TAG_GPS_ALTITUDE_REF,
            "GPS LatitudeRef" to ExifInterface.TAG_GPS_LATITUDE_REF,
            "GPS LongitudeRef" to ExifInterface.TAG_GPS_LONGITUDE_REF,
            "GPS AreaInformation" to ExifInterface.TAG_GPS_AREA_INFORMATION,
            "GPS Differential" to ExifInterface.TAG_GPS_DIFFERENTIAL,
            "GPS HPositioningError" to ExifInterface.TAG_GPS_H_POSITIONING_ERROR,
            
            // Copyright & Author
            "Artist" to ExifInterface.TAG_ARTIST,
            "Copyright" to ExifInterface.TAG_COPYRIGHT,
            
            // Comments & Descriptions
            "User Comment" to ExifInterface.TAG_USER_COMMENT,
            "Image Description" to ExifInterface.TAG_IMAGE_DESCRIPTION,
            "Maker Note" to ExifInterface.TAG_MAKER_NOTE,
            
            // Lens Information
            "Lens Make" to ExifInterface.TAG_LENS_MAKE,
            "Lens Model" to ExifInterface.TAG_LENS_MODEL,
            "Lens SerialNumber" to ExifInterface.TAG_LENS_SERIAL_NUMBER,
            "Lens Specification" to ExifInterface.TAG_LENS_SPECIFICATION,
            
            // Camera Settings
            "Focal Length" to ExifInterface.TAG_FOCAL_LENGTH,
            "Focal Length In 35mm" to ExifInterface.TAG_FOCAL_LENGTH_IN_35MM_FILM,
            "Aperture" to ExifInterface.TAG_APERTURE_VALUE,
            "FNumber" to ExifInterface.TAG_F_NUMBER,
            "Exposure Time" to ExifInterface.TAG_EXPOSURE_TIME,
            "Exposure Program" to ExifInterface.TAG_EXPOSURE_PROGRAM,
            "Exposure Mode" to ExifInterface.TAG_EXPOSURE_MODE,
            "Exposure Bias" to ExifInterface.TAG_EXPOSURE_BIAS_VALUE,
            "Exposure Index" to ExifInterface.TAG_EXPOSURE_INDEX,
            "ISO" to ExifInterface.TAG_ISO_SPEED,
            "ISO Speed" to ExifInterface.TAG_PHOTOGRAPHIC_SENSITIVITY,
            "ISO Speed Ratings" to ExifInterface.TAG_ISO_SPEED_RATINGS,
            "Sensitivity Type" to ExifInterface.TAG_SENSITIVITY_TYPE,
            "Recommended Exposure Index" to ExifInterface.TAG_RECOMMENDED_EXPOSURE_INDEX,
            "Standard Output Sensitivity" to ExifInterface.TAG_STANDARD_OUTPUT_SENSITIVITY,
            
            // Flash
            "Flash" to ExifInterface.TAG_FLASH,
            "Flash Energy" to ExifInterface.TAG_FLASH_ENERGY,
            
            // White Balance & Color
            "White Balance" to ExifInterface.TAG_WHITE_BALANCE,
            "Color Space" to ExifInterface.TAG_COLOR_SPACE,
            "Gamma" to ExifInterface.TAG_GAMMA,
            "Reference Black White" to ExifInterface.TAG_REFERENCE_BLACK_WHITE,
            
            // Metering & Focus
            "Metering Mode" to ExifInterface.TAG_METERING_MODE,
            "Subject Distance" to ExifInterface.TAG_SUBJECT_DISTANCE,
            "Subject Area" to ExifInterface.TAG_SUBJECT_AREA,
            "Subject Distance Range" to ExifInterface.TAG_SUBJECT_DISTANCE_RANGE,
            "Subject Location" to ExifInterface.TAG_SUBJECT_LOCATION,
            
            // Scene & Capture
            "Scene Capture Type" to ExifInterface.TAG_SCENE_CAPTURE_TYPE,
            "Scene Type" to ExifInterface.TAG_SCENE_TYPE,
            "Gain Control" to ExifInterface.TAG_GAIN_CONTROL,
            "Contrast" to ExifInterface.TAG_CONTRAST,
            "Saturation" to ExifInterface.TAG_SATURATION,
            "Sharpness" to ExifInterface.TAG_SHARPNESS,
            "Light Source" to ExifInterface.TAG_LIGHT_SOURCE,
            
            // Image Processing
            "Digital Zoom Ratio" to ExifInterface.TAG_DIGITAL_ZOOM_RATIO,
            "Brightness" to ExifInterface.TAG_BRIGHTNESS_VALUE,
            "Shutter Speed" to ExifInterface.TAG_SHUTTER_SPEED_VALUE,
            "Max Aperture" to ExifInterface.TAG_MAX_APERTURE_VALUE,
            "Compressed Bits Per Pixel" to ExifInterface.TAG_COMPRESSED_BITS_PER_PIXEL,
            
            // Thumbnail
            "Thumbnail Image Length" to ExifInterface.TAG_THUMBNAIL_IMAGE_LENGTH,
            "Thumbnail Image Width" to ExifInterface.TAG_THUMBNAIL_IMAGE_WIDTH,
            "Thumbnail Orientation" to ExifInterface.TAG_THUMBNAIL_ORIENTATION,
            "Thumbnail Compression" to "ThumbnailCompression",
            "JPEGInterchangeFormat" to ExifInterface.TAG_JPEG_INTERCHANGE_FORMAT,
            "JPEGInterchangeFormatLength" to ExifInterface.TAG_JPEG_INTERCHANGE_FORMAT_LENGTH,
            
            // Strip & Tile Info
            "Strip Offsets" to ExifInterface.TAG_STRIP_OFFSETS,
            "Strip Byte Counts" to ExifInterface.TAG_STRIP_BYTE_COUNTS,
            "Rows Per Strip" to ExifInterface.TAG_ROWS_PER_STRIP,
            
            // Transfer Function
            "Transfer Function" to ExifInterface.TAG_TRANSFER_FUNCTION,
            "Primary Chromaticities" to ExifInterface.TAG_PRIMARY_CHROMATICITIES,
            
            // Other Metadata
            "Device Setting Description" to ExifInterface.TAG_DEVICE_SETTING_DESCRIPTION,
            "CFA Pattern" to ExifInterface.TAG_CFA_PATTERN,
            "Spectral Sensitivity" to ExifInterface.TAG_SPECTRAL_SENSITIVITY,
            "OECF" to ExifInterface.TAG_OECF,
            "Spatial Frequency Response" to ExifInterface.TAG_SPATIAL_FREQUENCY_RESPONSE,
            "File Source" to ExifInterface.TAG_FILE_SOURCE,
            "Custom Rendered" to ExifInterface.TAG_CUSTOM_RENDERED,
            "New Subfile Type" to ExifInterface.TAG_NEW_SUBFILE_TYPE,
            "Subfile Type" to ExifInterface.TAG_SUBFILE_TYPE,
            
            // EXIF Version & Standards
            "EXIF Version" to ExifInterface.TAG_EXIF_VERSION,
            "Flashpix Version" to ExifInterface.TAG_FLASHPIX_VERSION,
            
            // Related Image Info
            "Related Sound File" to ExifInterface.TAG_RELATED_SOUND_FILE,
            "Image Unique ID" to ExifInterface.TAG_IMAGE_UNIQUE_ID,
            
            // DNG (RAW) specific
            "DNG Version" to ExifInterface.TAG_DNG_VERSION,
            "Default Crop Size" to ExifInterface.TAG_DEFAULT_CROP_SIZE,
            
            // XMP & IPTC related
            "XMP" to ExifInterface.TAG_XMP,
            "Rating" to "Rating",
            "RatingPercent" to "RatingPercent",
            
            // Interoperability
            "Interoperability Index" to ExifInterface.TAG_INTEROPERABILITY_INDEX,
            
            // Components Configuration
            "Components Configuration" to ExifInterface.TAG_COMPONENTS_CONFIGURATION
        )
        
        val removedFields = mutableListOf<String>()
        tags.forEach { (name, tag) ->
            try {
                if (exif.getAttribute(tag) != null) {
                    exif.setAttribute(tag, null)
                    removedFields.add(name)
                }
            } catch (e: Exception) {
                // Some tags might not be supported, continue with others
            }
        }
        
        // Additional cleanup: Remove all IFD data
        try {
            exif.setAttribute(ExifInterface.TAG_THUMBNAIL_IMAGE_LENGTH, null)
            exif.setAttribute(ExifInterface.TAG_THUMBNAIL_IMAGE_WIDTH, null)
        } catch (e: Exception) {
            // Ignore if not present
        }
        
        return RemovalResult(removedFields.size, removedFields)
    }
    
    private fun removeGpsData(exif: ExifInterface): RemovalResult {
        val gpsTags = listOf(
            "GPS Latitude" to ExifInterface.TAG_GPS_LATITUDE,
            "GPS Longitude" to ExifInterface.TAG_GPS_LONGITUDE,
            "GPS Altitude" to ExifInterface.TAG_GPS_ALTITUDE,
            "GPS Timestamp" to ExifInterface.TAG_GPS_TIMESTAMP,
            "GPS Date" to ExifInterface.TAG_GPS_DATESTAMP,
            "GPS Processing Method" to ExifInterface.TAG_GPS_PROCESSING_METHOD
        )
        
        val removedFields = mutableListOf<String>()
        gpsTags.forEach { (name, tag) ->
            if (exif.getAttribute(tag) != null) {
                exif.setAttribute(tag, null)
                removedFields.add(name)
            }
        }
        return RemovalResult(removedFields.size, removedFields)
    }
    
    private fun removeDateTimeData(exif: ExifInterface): RemovalResult {
        val dateTags = listOf(
            "DateTime" to ExifInterface.TAG_DATETIME,
            "DateTimeDigitized" to ExifInterface.TAG_DATETIME_DIGITIZED,
            "DateTimeOriginal" to ExifInterface.TAG_DATETIME_ORIGINAL
        )
        
        val removedFields = mutableListOf<String>()
        dateTags.forEach { (name, tag) ->
            if (exif.getAttribute(tag) != null) {
                exif.setAttribute(tag, null)
                removedFields.add(name)
            }
        }
        return RemovalResult(removedFields.size, removedFields)
    }
    
    private fun removeCameraData(exif: ExifInterface): RemovalResult {
        val cameraTags = listOf(
            "Camera Make" to ExifInterface.TAG_MAKE,
            "Camera Model" to ExifInterface.TAG_MODEL,
            "Lens Make" to ExifInterface.TAG_LENS_MAKE,
            "Lens Model" to ExifInterface.TAG_LENS_MODEL
        )
        
        val removedFields = mutableListOf<String>()
        cameraTags.forEach { (name, tag) ->
            if (exif.getAttribute(tag) != null) {
                exif.setAttribute(tag, null)
                removedFields.add(name)
            }
        }
        return RemovalResult(removedFields.size, removedFields)
    }
    
    private fun removeSoftwareData(exif: ExifInterface): RemovalResult {
        val softwareTags = listOf(
            "Software" to ExifInterface.TAG_SOFTWARE,
            "Artist" to ExifInterface.TAG_ARTIST,
            "Copyright" to ExifInterface.TAG_COPYRIGHT
        )
        
        val removedFields = mutableListOf<String>()
        softwareTags.forEach { (name, tag) ->
            if (exif.getAttribute(tag) != null) {
                exif.setAttribute(tag, null)
                removedFields.add(name)
            }
        }
        return RemovalResult(removedFields.size, removedFields)
    }
}
