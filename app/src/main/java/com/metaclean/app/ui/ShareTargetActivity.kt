package com.metaclean.app.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.metaclean.app.ui.theme.MetaCleanTheme
import kotlinx.coroutines.launch

class ShareTargetActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val imageUris = when (intent.action) {
            Intent.ACTION_SEND -> {
                listOfNotNull(intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM))
            }
            Intent.ACTION_SEND_MULTIPLE -> {
                intent.getParcelableArrayListExtra<Uri>(Intent.EXTRA_STREAM) ?: emptyList()
            }
            else -> emptyList()
        }
        
        setContent {
            MetaCleanTheme {
                ShareTargetScreen(
                    imageUris = imageUris,
                    onComplete = { finish() }
                )
            }
        }
    }
}

@Composable
fun ShareTargetScreen(
    imageUris: List<Uri>,
    onComplete: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var processing by remember { mutableStateOf(false) }
    
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Clean Metadata",
                style = MaterialTheme.typography.headlineMedium
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "${imageUris.size} file(s) selected",
                style = MaterialTheme.typography.bodyLarge
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            if (processing) {
                CircularProgressIndicator()
            } else {
                Button(
                    onClick = {
                        scope.launch {
                            processing = true
                            // Process images here
                            kotlinx.coroutines.delay(2000)
                            processing = false
                            onComplete()
                        }
                    }
                ) {
                    Text("Remove All Metadata")
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                TextButton(onClick = onComplete) {
                    Text("Cancel")
                }
            }
        }
    }
}
