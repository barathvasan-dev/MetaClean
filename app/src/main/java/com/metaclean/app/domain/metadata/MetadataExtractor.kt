package com.metaclean.app.domain.metadata

import android.content.Context
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import com.drew.imaging.ImageMetadataReader
import com.drew.metadata.exif.ExifIFD0Directory
import com.drew.metadata.exif.ExifSubIFDDirectory
import com.drew.metadata.exif.GpsDirectory
import com.metaclean.app.domain.model.MetadataInfo
import java.io.File

class MetadataExtractor(private val context: Context) {
    
    fun extractMetadata(uri: Uri): MetadataInfo? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            
            val metadata = ImageMetadataReader.readMetadata(inputStream)
            val exifIFD0 = metadata.getFirstDirectoryOfType(ExifIFD0Directory::class.java)
            val exifSubIFD = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory::class.java)
            val gpsDirectory = metadata.getFirstDirectoryOfType(GpsDirectory::class.java)
            
            val exifData = mutableMapOf<String, String>()
            val gpsData = mutableMapOf<String, String>()
            
            // Extract EXIF data
            exifIFD0?.tags?.forEach { tag ->
                exifData[tag.tagName] = tag.description ?: ""
            }
            exifSubIFD?.tags?.forEach { tag ->
                exifData[tag.tagName] = tag.description ?: ""
            }
            
            // Extract GPS data
            gpsDirectory?.tags?.forEach { tag ->
                gpsData[tag.tagName] = tag.description ?: ""
            }
            
            val latitude = gpsDirectory?.geoLocation?.latitude
            val longitude = gpsDirectory?.geoLocation?.longitude
            
            // Get file info
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            var fileName = ""
            var fileSize = 0L
            cursor?.use {
                if (it.moveToFirst()) {
                    val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    val sizeIndex = it.getColumnIndex(android.provider.OpenableColumns.SIZE)
                    if (nameIndex >= 0) fileName = it.getString(nameIndex)
                    if (sizeIndex >= 0) fileSize = it.getLong(sizeIndex)
                }
            }
            
            MetadataInfo(
                filePath = uri.toString(),
                fileName = fileName,
                fileSize = fileSize,
                mimeType = context.contentResolver.getType(uri) ?: "unknown",
                exifData = exifData,
                iptcData = emptyMap(), // Can be extended
                xmpData = emptyMap(), // Can be extended
                hasGps = gpsDirectory != null && latitude != null && longitude != null,
                latitude = latitude,
                longitude = longitude,
                dateTimeTaken = exifSubIFD?.getString(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL),
                cameraModel = exifIFD0?.getString(ExifIFD0Directory.TAG_MODEL),
                cameraManufacturer = exifIFD0?.getString(ExifIFD0Directory.TAG_MAKE),
                lens = exifSubIFD?.getString(ExifSubIFDDirectory.TAG_LENS_MODEL),
                iso = exifSubIFD?.getString(ExifSubIFDDirectory.TAG_ISO_EQUIVALENT),
                shutterSpeed = exifSubIFD?.getString(ExifSubIFDDirectory.TAG_SHUTTER_SPEED),
                aperture = exifSubIFD?.getString(ExifSubIFDDirectory.TAG_APERTURE),
                focalLength = exifSubIFD?.getString(ExifSubIFDDirectory.TAG_FOCAL_LENGTH),
                software = exifIFD0?.getString(ExifIFD0Directory.TAG_SOFTWARE)
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    fun extractMetadataFromFile(file: File): MetadataInfo? {
        return try {
            val exif = ExifInterface(file.absolutePath)
            val exifData = mutableMapOf<String, String>()
            
            // Extract common EXIF tags
            val tags = listOf(
                ExifInterface.TAG_DATETIME,
                ExifInterface.TAG_MAKE,
                ExifInterface.TAG_MODEL,
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.TAG_IMAGE_WIDTH,
                ExifInterface.TAG_IMAGE_LENGTH,
                ExifInterface.TAG_GPS_LATITUDE,
                ExifInterface.TAG_GPS_LONGITUDE,
                ExifInterface.TAG_GPS_ALTITUDE,
                ExifInterface.TAG_FOCAL_LENGTH,
                ExifInterface.TAG_APERTURE_VALUE,
                ExifInterface.TAG_EXPOSURE_TIME,
                ExifInterface.TAG_ISO_SPEED,
                ExifInterface.TAG_LENS_MODEL,
                ExifInterface.TAG_SOFTWARE
            )
            
            tags.forEach { tag ->
                exif.getAttribute(tag)?.let { value ->
                    exifData[tag] = value
                }
            }
            
            val latLong = exif.latLong
            
            MetadataInfo(
                filePath = file.absolutePath,
                fileName = file.name,
                fileSize = file.length(),
                mimeType = getMimeType(file.extension),
                exifData = exifData,
                hasGps = latLong != null,
                latitude = latLong?.get(0),
                longitude = latLong?.get(1),
                dateTimeTaken = exif.getAttribute(ExifInterface.TAG_DATETIME),
                cameraModel = exif.getAttribute(ExifInterface.TAG_MODEL),
                cameraManufacturer = exif.getAttribute(ExifInterface.TAG_MAKE),
                lens = exif.getAttribute(ExifInterface.TAG_LENS_MODEL),
                iso = exif.getAttribute(ExifInterface.TAG_ISO_SPEED),
                shutterSpeed = exif.getAttribute(ExifInterface.TAG_EXPOSURE_TIME),
                aperture = exif.getAttribute(ExifInterface.TAG_APERTURE_VALUE),
                focalLength = exif.getAttribute(ExifInterface.TAG_FOCAL_LENGTH),
                software = exif.getAttribute(ExifInterface.TAG_SOFTWARE)
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    private fun getMimeType(extension: String): String {
        return when (extension.lowercase()) {
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "heic" -> "image/heic"
            "webp" -> "image/webp"
            "dng", "raw" -> "image/x-adobe-dng"
            "mp4" -> "video/mp4"
            "mov" -> "video/quicktime"
            else -> "application/octet-stream"
        }
    }
}
