package com.metaclean.app.ui.screen.clean

import android.Manifest
import android.content.ContentValues
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import androidx.navigation.NavController
import com.metaclean.app.MetaCleanApplication
import com.metaclean.app.domain.model.CleaningPreset
import com.metaclean.app.domain.model.CleaningResult
import com.metaclean.app.domain.metadata.MetadataCleaner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun CleanScreen(navController: NavController? = null, initialPreset: String? = null) {
    val context = LocalContext.current
    val app = context.applicationContext as MetaCleanApplication
    val scope = rememberCoroutineScope()
    
    // Determine initial preset based on navigation argument
    val startPreset = when(initialPreset) {
        "ANONYMOUS" -> CleaningPreset.ANONYMOUS
        "GPS_ONLY" -> CleaningPreset.GPS_ONLY
        "SOCIAL_SAFE" -> CleaningPreset.SOCIAL_SAFE
        "PROFESSIONAL" -> CleaningPreset.PROFESSIONAL
        else -> CleaningPreset.SOCIAL_SAFE
    }
    
    var selectedImages by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var selectedPreset by remember { mutableStateOf(startPreset) }
    var showCustomDialog by remember { mutableStateOf(false) }
    var customRemoveGps by remember { mutableStateOf(true) }
    var customRemoveDateTime by remember { mutableStateOf(true) }
    var customRemoveCameraInfo by remember { mutableStateOf(true) }
    var customRemoveSoftware by remember { mutableStateOf(true) }
    var isProcessing by remember { mutableStateOf(false) }
    var showPresetDialog by remember { mutableStateOf(false) }
    var cleaningResults by remember { mutableStateOf<List<CleaningResult>>(emptyList()) }
    var showResultsDialog by remember { mutableStateOf(false) }
    var processingProgress by remember { mutableStateOf("") }
    var selectedImageForView by remember { mutableStateOf<Uri?>(null) }
    var showMetadataDialog by remember { mutableStateOf(false) }
    
    // Permission handling
    val permissionState = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        rememberMultiplePermissionsState(
            listOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO
            )
        )
    } else {
        rememberMultiplePermissionsState(
            listOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        )
    }
    
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        selectedImages = uris
    }
    
    // Main UI
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        CleanScreenContent(
            permissionState = permissionState,
            selectedImages = selectedImages,
            onSelectedImagesChange = { selectedImages = it },
            selectedPreset = selectedPreset,
            onPresetChange = { selectedPreset = it },
            showPresetDialog = showPresetDialog,
            onShowPresetDialogChange = { showPresetDialog = it },
            imagePicker = imagePicker,
            isProcessing = isProcessing,
            onProcessingChange = { isProcessing = it },
            processingProgress = processingProgress,
            onProcessingProgressChange = { processingProgress = it },
            selectedImageForView = selectedImageForView,
            onSelectedImageForViewChange = { selectedImageForView = it },
            showMetadataDialog = showMetadataDialog,
            onShowMetadataDialogChange = { showMetadataDialog = it },
            showCustomDialog = showCustomDialog,
            onShowCustomDialogChange = { showCustomDialog = it },
            customRemoveGps = customRemoveGps,
            onCustomRemoveGpsChange = { customRemoveGps = it },
            customRemoveDateTime = customRemoveDateTime,
            onCustomRemoveDateTimeChange = { customRemoveDateTime = it },
            customRemoveCameraInfo = customRemoveCameraInfo,
            onCustomRemoveCameraInfoChange = { customRemoveCameraInfo = it },
            customRemoveSoftware = customRemoveSoftware,
            onCustomRemoveSoftwareChange = { customRemoveSoftware = it },
            showResultsDialog = showResultsDialog,
            onShowResultsDialogChange = { showResultsDialog = it },
            cleaningResults = cleaningResults,
            onCleaningResultsChange = { cleaningResults = it },
            context = context,
            app = app,
            scope = scope
        )
    }
}

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun CleanScreenContent(
    permissionState: com.google.accompanist.permissions.MultiplePermissionsState,
    selectedImages: List<Uri>,
    onSelectedImagesChange: (List<Uri>) -> Unit,
    selectedPreset: CleaningPreset,
    onPresetChange: (CleaningPreset) -> Unit,
    showPresetDialog: Boolean,
    onShowPresetDialogChange: (Boolean) -> Unit,
    imagePicker: androidx.activity.compose.ManagedActivityResultLauncher<String, List<@JvmSuppressWildcards Uri>>,
    isProcessing: Boolean,
    onProcessingChange: (Boolean) -> Unit,
    processingProgress: String,
    onProcessingProgressChange: (String) -> Unit,
    selectedImageForView: Uri?,
    onSelectedImageForViewChange: (Uri?) -> Unit,
    showMetadataDialog: Boolean,
    onShowMetadataDialogChange: (Boolean) -> Unit,
    showCustomDialog: Boolean,
    onShowCustomDialogChange: (Boolean) -> Unit,
    customRemoveGps: Boolean,
    onCustomRemoveGpsChange: (Boolean) -> Unit,
    customRemoveDateTime: Boolean,
    onCustomRemoveDateTimeChange: (Boolean) -> Unit,
    customRemoveCameraInfo: Boolean,
    onCustomRemoveCameraInfoChange: (Boolean) -> Unit,
    customRemoveSoftware: Boolean,
    onCustomRemoveSoftwareChange: (Boolean) -> Unit,
    showResultsDialog: Boolean,
    onShowResultsDialogChange: (Boolean) -> Unit,
    cleaningResults: List<CleaningResult>,
    onCleaningResultsChange: (List<CleaningResult>) -> Unit,
    context: android.content.Context,
    app: MetaCleanApplication,
    scope: kotlinx.coroutines.CoroutineScope
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Permission check
        if (!permissionState.allPermissionsGranted) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Storage Permission Required",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "MetaClean needs access to your photos to view and clean metadata.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { permissionState.launchMultiplePermissionRequest() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Grant Permission")
                    }
                }
            }
        } else {
            
            // Preset Selection
            Card(
                modifier = Modifier.fillMaxWidth(),
                onClick = { onShowPresetDialogChange(true) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Cleaning Preset",
                            style = MaterialTheme.typography.labelMedium
                        )
                        Text(
                            text = selectedPreset.displayName,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = selectedPreset.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(Icons.Default.ArrowDropDown, "Change preset")
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Select Images Button
            Button(
                onClick = { imagePicker.launch("image/*") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.PhotoLibrary, "Select images")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Select Images (${selectedImages.size})")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Processing indicator
            if (isProcessing) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(processingProgress)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Selected Images List
            if (selectedImages.isNotEmpty()) {
                Card(modifier = Modifier.weight(1f)) {
                    LazyColumn(
                        modifier = Modifier.padding(8.dp)
                    ) {
                        items(selectedImages) { uri ->
                            ListItem(
                                headlineContent = { 
                                    Text(uri.lastPathSegment ?: "Unknown") 
                                },
                                leadingContent = {
                                    Icon(Icons.Default.Image, null)
                                },
                                trailingContent = {
                                    IconButton(
                                        onClick = {
                                            onSelectedImagesChange(selectedImages.filter { it != uri })
                                        }
                                    ) {
                                        Icon(Icons.Default.Close, "Remove")
                                    }
                                }
                            )
                            Divider()
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // View Metadata Button
            if (selectedImages.isNotEmpty()) {
                OutlinedButton(
                    onClick = {
                        onSelectedImageForViewChange(selectedImages.firstOrNull())
                        onShowMetadataDialogChange(true)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Visibility, "View")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("View Metadata")
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            // Clean Button
            Button(
                onClick = {
                    scope.launch {
                        onProcessingChange(true)
                        onCleaningResultsChange(emptyList())
                        
                        val results = mutableListOf<CleaningResult>()
                        val cleaner = MetadataCleaner(context)
                        
                        // Determine which preset to use
                        val effectivePreset = selectedPreset
                        
                        withContext(Dispatchers.IO) {
                            selectedImages.forEachIndexed { index, uri ->
                                withContext(Dispatchers.Main) {
                                    onProcessingProgressChange("Cleaning ${index + 1} of ${selectedImages.size}...")
                                }
                                
                                try {
                                    // Create output file in Pictures/MetaClean folder
                                    val timestamp = System.currentTimeMillis()
                                    val fileName = "MetaClean_$timestamp.jpg"
                                    
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                        // Use MediaStore for Android 10+
                                        val contentValues = ContentValues().apply {
                                            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                                            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                                            put(MediaStore.Images.Media.RELATIVE_PATH, 
                                                "${Environment.DIRECTORY_PICTURES}/MetaClean")
                                        }
                                        
                                        val outputUri = context.contentResolver.insert(
                                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                            contentValues
                                        )
                                        
                                        if (outputUri != null) {
                                            // Copy input to temp file
                                            val tempFile = File(context.cacheDir, "temp_$timestamp.jpg")
                                            context.contentResolver.openInputStream(uri)?.use { input ->
                                                tempFile.outputStream().use { output ->
                                                    input.copyTo(output)
                                                }
                                            }
                                            
                                            // Clean the temp file
                                            val cleanedTemp = File(context.cacheDir, "cleaned_$timestamp.jpg")
                                            val result = cleaner.cleanMetadataFromFile(
                                                tempFile,
                                                effectivePreset,
                                                cleanedTemp,
                                                if (effectivePreset == CleaningPreset.CUSTOM) customRemoveGps else null,
                                                if (effectivePreset == CleaningPreset.CUSTOM) customRemoveDateTime else null,
                                                if (effectivePreset == CleaningPreset.CUSTOM) customRemoveCameraInfo else null,
                                                if (effectivePreset == CleaningPreset.CUSTOM) customRemoveSoftware else null
                                            )
                                            
                                            // Copy cleaned file to output URI
                                            if (result.success && cleanedTemp.exists()) {
                                                context.contentResolver.openOutputStream(outputUri)?.use { output ->
                                                    cleanedTemp.inputStream().use { input ->
                                                        input.copyTo(output)
                                                    }
                                                }
                                            }
                                            
                                            tempFile.delete()
                                            cleanedTemp.delete()
                                            
                                            results.add(result.copy(cleanedPath = fileName))
                                        }
                                    } else {
                                        // For older Android versions
                                        val picturesDir = Environment.getExternalStoragePublicDirectory(
                                            Environment.DIRECTORY_PICTURES
                                        )
                                        val metaCleanDir = File(picturesDir, "MetaClean")
                                        metaCleanDir.mkdirs()
                                        
                                        val outputFile = File(metaCleanDir, fileName)
                                        val result = cleaner.cleanMetadata(
                                            uri,
                                            selectedPreset,
                                            outputFile,
                                            false
                                        )
                                        results.add(result)
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    results.add(
                                        CleaningResult(
                                            success = false,
                                            originalPath = uri.toString(),
                                            originalSize = 0,
                                            errorMessage = e.message
                                        )
                                    )
                                }
                            }
                        }
                        
                        onCleaningResultsChange(results)
                        
                        // Save to history
                        results.forEach { result ->
                            scope.launch {
                                app.historyRepository.addToHistory(result)
                            }
                        }
                        
                        onProcessingChange(false)
                        onShowResultsDialogChange(true)
                        
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                context,
                                "Cleaned ${results.count { it.success }} of ${results.size} files",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = selectedImages.isNotEmpty() && !isProcessing
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(Icons.Default.CleaningServices, "Clean")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isProcessing) "Cleaning..." else "Clean Metadata")
            }
        }
    }
    
    // Preset Selection Dialog
    if (showPresetDialog) {
        AlertDialog(
            onDismissRequest = { onShowPresetDialogChange(false) },
            title = { Text("Select Cleaning Preset") },
            text = {
                LazyColumn {
                    items(CleaningPreset.values().toList()) { preset ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            onClick = {
                                if (preset == CleaningPreset.CUSTOM) {
                                    onShowPresetDialogChange(false)
                                    onShowCustomDialogChange(true)
                                } else {
                                    onPresetChange(preset)
                                    onShowPresetDialogChange(false)
                                }
                            },
                            colors = if (preset == selectedPreset) {
                                CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            } else {
                                CardDefaults.cardColors()
                            }
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = preset.displayName,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = preset.description,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { onShowPresetDialogChange(false) }) {
                    Text("Close")
                }
            }
        )
    }
    
    // Custom Preset Dialog
    if (showCustomDialog) {
        AlertDialog(
            onDismissRequest = { onShowCustomDialogChange(false) },
            title = { Text("Custom Cleaning Options") },
            text = {
                Column {
                    Text(
                        "Select which metadata fields to remove:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = customRemoveGps,
                            onCheckedChange = { onCustomRemoveGpsChange(it) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("GPS Location Data")
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = customRemoveDateTime,
                            onCheckedChange = { onCustomRemoveDateTimeChange(it) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Date & Time Information")
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = customRemoveCameraInfo,
                            onCheckedChange = { onCustomRemoveCameraInfoChange(it) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Camera & Lens Info")
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = customRemoveSoftware,
                            onCheckedChange = { onCustomRemoveSoftwareChange(it) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Software & Artist Info")
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    onPresetChange(CleaningPreset.CUSTOM)
                    onShowCustomDialogChange(false)
                }) {
                    Text("Apply")
                }
            },
            dismissButton = {
                TextButton(onClick = { onShowCustomDialogChange(false) }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Results Dialog
    if (showResultsDialog) {
        AlertDialog(
            onDismissRequest = { onShowResultsDialogChange(false) },
            icon = { 
                Icon(
                    if (cleaningResults.all { it.success }) Icons.Default.CheckCircle 
                    else Icons.Default.Warning,
                    contentDescription = null,
                    tint = if (cleaningResults.all { it.success }) 
                        MaterialTheme.colorScheme.primary 
                    else MaterialTheme.colorScheme.error
                )
            },
            title = { Text("Cleaning Complete") },
            text = {
                LazyColumn {
                    item {
                        Text(
                            text = "✓ ${cleaningResults.count { it.success }} succeeded\n" +
                                   "✗ ${cleaningResults.count { !it.success }} failed\n\n" +
                                   "Files saved to Pictures/MetaClean/",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    
                    items(cleaningResults) { result ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (result.success)
                                    MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        if (result.success) Icons.Default.Check else Icons.Default.Error,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = result.cleanedPath ?: "Unknown",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        if (result.success) {
                                            Text(
                                                text = "${result.metadataRemoved} fields removed",
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        } else {
                                            Text(
                                                text = result.errorMessage ?: "Error",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    onShowResultsDialogChange(false)
                    onSelectedImagesChange(emptyList())
                }) {
                    Text("Done")
                }
            }
        )
    }
    
    // Metadata View Dialog
    if (showMetadataDialog && selectedImageForView != null) {
        val metadataExtractor = remember { com.metaclean.app.domain.metadata.MetadataExtractor(context) }
        val metadata = remember(selectedImageForView) {
            metadataExtractor.extractMetadata(selectedImageForView!!)
        }
        
        AlertDialog(
            onDismissRequest = { onShowMetadataDialogChange(false) },
            title = { Text("Metadata Information") },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        "File: ${selectedImageForView!!.lastPathSegment}\n",
                        style = MaterialTheme.typography.titleSmall
                    )
                    
                    if (metadata?.exifData?.isNotEmpty() == true) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("EXIF Data:", style = MaterialTheme.typography.titleSmall)
                        metadata.exifData?.entries?.forEach { (key, value) ->
                            Text("$key: $value", style = MaterialTheme.typography.bodySmall)
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }
                    
                    if (metadata?.hasGps == true && (metadata.latitude != null || metadata.longitude != null)) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("GPS Location:", style = MaterialTheme.typography.titleSmall)
                        metadata.latitude?.let { lat ->
                            Text("Latitude: $lat", style = MaterialTheme.typography.bodySmall)
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                        metadata.longitude?.let { lon ->
                            Text("Longitude: $lon", style = MaterialTheme.typography.bodySmall)
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }
                    
                    if (metadata?.exifData?.isEmpty() == true && metadata.hasGps == false) {
                        Text("No metadata found", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            },
            confirmButton = {
                Button(onClick = { onShowMetadataDialogChange(false) }) {
                    Text("Close")
                }
            }
        )
    }
}
