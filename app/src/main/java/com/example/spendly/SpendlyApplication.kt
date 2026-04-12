package com.example.spendly

import android.app.Application
import com.example.spendly.data.db.AppDatabase
import com.example.spendly.data.preferences.UserPreferencesRepository
import com.example.spendly.data.repository.SpendlyRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class SpendlyApplication : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    lateinit var database: AppDatabase
        private set
    lateinit var repository: SpendlyRepository
        private set

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.build(this)
        val prefs = UserPreferencesRepository(this)
        repository = SpendlyRepository(database, prefs)
        applicationScope.launch(Dispatchers.IO) {
            repository.ensureDefaultData()
        }
    }
}
