package com.metaclean.app.ui.screen.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.metaclean.app.MetaCleanApplication
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HistoryScreen() {
    val context = LocalContext.current
    val app = context.applicationContext as MetaCleanApplication
    val scope = rememberCoroutineScope()
    
    val historyItems by app.historyRepository.history.collectAsStateWithLifecycle(
        initialValue = emptyList()
    )
    
    var showClearDialog by remember { mutableStateOf(false) }
    var selectedItemForDetails by remember { mutableStateOf<com.metaclean.app.domain.model.CleaningResult?>(null) }
    var showDetailsDialog by remember { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()) }
    
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header with clear button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Cleaning History",
                    style = MaterialTheme.typography.titleLarge
                )
                if (historyItems.isNotEmpty()) {
                    TextButton(onClick = { showClearDialog = true }) {
                        Text("Clear All")
                    }
                }
            }
            
            // History list or empty state
            if (historyItems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No history yet",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Cleaned files will appear here",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(historyItems) { item ->
                        @OptIn(ExperimentalMaterial3Api::class)
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                selectedItemForDetails = item
                                showDetailsDialog = true
                            },
                            colors = CardDefaults.cardColors(
                                containerColor = if (item.success)
                                    MaterialTheme.colorScheme.surfaceVariant
                                else MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (item.success) 
                                        Icons.Default.CheckCircle 
                                    else Icons.Default.Error,
                                    contentDescription = null,
                                    modifier = Modifier.size(32.dp),
                                    tint = if (item.success)
                                        MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.error
                                )
                                
                                Spacer(modifier = Modifier.width(16.dp))
                                
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.cleanedPath?.substringAfterLast("/") ?: "Unknown",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(
                                        text = dateFormat.format(Date(item.timestamp)),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    if (item.success) {
                                        Text(
                                            text = "${item.metadataRemoved} fields removed • Tap for details",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    } else {
                                        Text(
                                            text = item.errorMessage ?: "Error",
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
        }
    }
    
    // Clear confirmation dialog
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            icon = { Icon(Icons.Default.DeleteForever, null) },
            title = { Text("Clear History?") },
            text = { Text("This will remove all history records. This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            app.historyRepository.clearHistory()
                        }
                        showClearDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Clear")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Details dialog
    if (showDetailsDialog && selectedItemForDetails != null) {
        val item = selectedItemForDetails!!
        AlertDialog(
            onDismissRequest = { showDetailsDialog = false },
            title = { Text("Cleaning Details") },
            text = {
                LazyColumn {
                    item {
                        Text(
                            "File: ${item.cleanedPath?.substringAfterLast("/") ?: "Unknown"}",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            "Date: ${dateFormat.format(Date(item.timestamp))}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // File sizes
                        Text("Original Size: ${formatFileSize(item.originalSize)}", style = MaterialTheme.typography.bodyMedium)
                        if (item.cleanedSize != null) {
                            Text("Cleaned Size: ${formatFileSize(item.cleanedSize)}", style = MaterialTheme.typography.bodyMedium)
                            val savings = item.originalSize - item.cleanedSize
                            if (savings > 0) {
                                Text("Space Saved: ${formatFileSize(savings)}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Dimensions
                        if (item.originalWidth > 0 && item.originalHeight > 0) {
                            Text("Original Size: ${item.originalWidth} x ${item.originalHeight} px", style = MaterialTheme.typography.bodyMedium)
                            val originalRatio = String.format("%.2f", item.originalWidth.toFloat() / item.originalHeight.toFloat())
                            Text("Original Ratio: $originalRatio:1", style = MaterialTheme.typography.bodyMedium)
                            
                            if (item.cleanedWidth > 0 && item.cleanedHeight > 0) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Cleaned Size: ${item.cleanedWidth} x ${item.cleanedHeight} px", style = MaterialTheme.typography.bodyMedium)
                                val cleanedRatio = String.format("%.2f", item.cleanedWidth.toFloat() / item.cleanedHeight.toFloat())
                                Text("Cleaned Ratio: $cleanedRatio:1", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Removed fields
                        if (item.fieldsRemoved.isNotEmpty()) {
                            Text("Metadata Fields Removed (${item.fieldsRemoved.size}):", style = MaterialTheme.typography.titleSmall)
                            Spacer(modifier = Modifier.height(4.dp))
                            item.fieldsRemoved.forEach { field ->
                                Text("• $field", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                            }
                        } else {
                            Text("No metadata fields removed", style = MaterialTheme.typography.bodyMedium)
                        }
                        
                        if (item.errorMessage != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Error: ${item.errorMessage}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showDetailsDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

private fun formatFileSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> String.format("%.2f KB", bytes / 1024.0)
        else -> String.format("%.2f MB", bytes / (1024.0 * 1024.0))
    }
}
