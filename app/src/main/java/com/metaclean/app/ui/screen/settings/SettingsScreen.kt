package com.metaclean.app.ui.screen.settings

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.metaclean.app.MetaCleanApplication
import com.metaclean.app.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val app = context.applicationContext as MetaCleanApplication
    val scope = rememberCoroutineScope()
    
    val isDarkMode by app.preferencesRepository.isDarkMode.collectAsStateWithLifecycle(false)
    val isAppLockEnabled by app.preferencesRepository.isAppLockEnabled.collectAsStateWithLifecycle(false)
    val saveAsCopy by app.preferencesRepository.saveAsCopy.collectAsStateWithLifecycle(true)
    val colorTheme by app.preferencesRepository.appColorTheme.collectAsStateWithLifecycle("yellow")
    
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                
                // Privacy Section
                SettingSectionHeader("Privacy & Security")
                
                SettingItem(
                    icon = Icons.Default.Lock,
                    title = "App Lock",
                    description = "Require biometric authentication to open app",
                    trailing = {
                        Switch(
                            checked = isAppLockEnabled,
                            onCheckedChange = { 
                                scope.launch { 
                                    app.preferencesRepository.setAppLock(it) 
                                }
                            }
                        )
                    }
                )
                
                SettingItem(
                    icon = Icons.Default.CloudOff,
                    title = "Offline Mode",
                    description = "No data leaves your device (always enabled)",
                    trailing = {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                )
                
                Divider(modifier = Modifier.padding(horizontal = 16.dp))
                
                // Appearance Section
                SettingSectionHeader("Appearance")
                
                SettingItem(
                    icon = Icons.Default.DarkMode,
                    title = "Dark Mode",
                    description = "Use dark theme",
                    trailing = {
                        Switch(
                            checked = isDarkMode,
                            onCheckedChange = { 
                                scope.launch { 
                                    app.preferencesRepository.setDarkMode(it) 
                                }
                            }
                        )
                    }
                )
                
                // Color Theme Selection
                Surface(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = "App Color Theme",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = "Choose your preferred color",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Color circles
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            ColorCircle(
                                color = YellowPrimary,
                                label = "Yellow",
                                isSelected = colorTheme == "yellow",
                                onClick = {
                                    scope.launch {
                                        app.preferencesRepository.setAppColorTheme("yellow")
                                    }
                                }
                            )
                            ColorCircle(
                                color = GreenPrimary,
                                label = "Green",
                                isSelected = colorTheme == "green",
                                onClick = {
                                    scope.launch {
                                        app.preferencesRepository.setAppColorTheme("green")
                                    }
                                }
                            )
                            ColorCircle(
                                color = BluePrimary,
                                label = "Blue",
                                isSelected = colorTheme == "blue",
                                onClick = {
                                    scope.launch {
                                        app.preferencesRepository.setAppColorTheme("blue")
                                    }
                                }
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            ColorCircle(
                                color = PurplePrimary,
                                label = "Purple",
                                isSelected = colorTheme == "purple",
                                onClick = {
                                    scope.launch {
                                        app.preferencesRepository.setAppColorTheme("purple")
                                    }
                                }
                            )
                            ColorCircle(
                                color = RedPrimary,
                                label = "Red",
                                isSelected = colorTheme == "red",
                                onClick = {
                                    scope.launch {
                                        app.preferencesRepository.setAppColorTheme("red")
                                    }
                                }
                            )
                            ColorCircle(
                                color = OrangePrimary,
                                label = "Orange",
                                isSelected = colorTheme == "orange",
                                onClick = {
                                    scope.launch {
                                        app.preferencesRepository.setAppColorTheme("orange")
                                    }
                                }
                            )
                        }
                    }
                }
                
                Divider(modifier = Modifier.padding(horizontal = 16.dp))
                
                // File Options Section
                SettingSectionHeader("File Options")
                
                SettingItem(
                    icon = Icons.Default.FileCopy,
                    title = "Save as Copy",
                    description = "Always save cleaned files as new copies",
                    trailing = {
                        Switch(
                            checked = saveAsCopy,
                            onCheckedChange = { 
                                scope.launch { 
                                    app.preferencesRepository.setSaveAsCopy(it) 
                                }
                            }
                        )
                    }
                )
                
                Divider(modifier = Modifier.padding(horizontal = 16.dp))
                
                // About Section
                SettingSectionHeader("About")
                
                SettingItem(
                    icon = Icons.Default.Info,
                    title = "Version",
                    description = "1.0.0"
                )
                
                SettingItem(
                    icon = Icons.Default.Shield,
                    title = "Privacy Guarantee",
                    description = "100% offline - zero data collection"
                )
                
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun SettingSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
fun SettingItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    trailing: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        onClick = { onClick?.invoke() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (trailing != null) {
                Spacer(modifier = Modifier.width(16.dp))
                trailing()
            }
        }
    }
}

@Composable
fun ColorCircle(
    color: Color,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val scale by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isSelected) 1.1f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        )
    )
    
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
        animationSpec = tween(300)
    )
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .scale(scale)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(color)
                .border(
                    width = if (isSelected) 3.dp else 1.dp,
                    color = borderColor,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
