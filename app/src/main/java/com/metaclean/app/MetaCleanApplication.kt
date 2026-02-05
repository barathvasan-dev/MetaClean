package com.metaclean.app

import android.app.Application
import com.metaclean.app.data.repository.PreferencesRepository
import com.metaclean.app.data.repository.HistoryRepository

class MetaCleanApplication : Application() {
    lateinit var preferencesRepository: PreferencesRepository
    lateinit var historyRepository: HistoryRepository
    
    override fun onCreate() {
        super.onCreate()
        preferencesRepository = PreferencesRepository(this)
        historyRepository = HistoryRepository(this)
    }
}
