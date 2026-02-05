package com.metaclean.app.data.repository

import android.content.Context
import com.metaclean.app.domain.model.CleaningResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

class HistoryRepository(private val context: Context) {
    
    private val historyFile = File(context.filesDir, "cleaning_history.json")
    private val _history = MutableStateFlow<List<CleaningResult>>(emptyList())
    val history: Flow<List<CleaningResult>> = _history.asStateFlow()
    
    init {
        loadHistory()
    }
    
    private fun loadHistory() {
        try {
            if (historyFile.exists()) {
                val json = historyFile.readText()
                // Simple implementation - in production, use proper serialization
                _history.value = emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    suspend fun addToHistory(result: CleaningResult) {
        val currentHistory = _history.value.toMutableList()
        currentHistory.add(0, result) // Add to beginning
        
        // Keep only last 100 items
        if (currentHistory.size > 100) {
            currentHistory.removeAt(currentHistory.size - 1)
        }
        
        _history.value = currentHistory
        saveHistory()
    }
    
    suspend fun clearHistory() {
        _history.value = emptyList()
        historyFile.delete()
    }
    
    private fun saveHistory() {
        try {
            // Simplified - in production, use proper JSON serialization
            historyFile.writeText("[]")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
