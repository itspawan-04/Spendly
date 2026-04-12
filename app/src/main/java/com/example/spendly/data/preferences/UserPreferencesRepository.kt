package com.example.spendly.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "spendly_user")

class UserPreferencesRepository(private val context: Context) {
    private object Keys {
        val sessionEmail = stringPreferencesKey("session_email")
        val displayName = stringPreferencesKey("display_name")
        val currencyCode = stringPreferencesKey("currency_code")
        val onboardingComplete = booleanPreferencesKey("onboarding_complete")
        val notificationsEnabled = booleanPreferencesKey("notifications_enabled")
        val monthlyBudgetMinor = longPreferencesKey("monthly_budget_minor")
        val dailyLimitMinor = longPreferencesKey("daily_limit_minor")
    }

    val preferencesFlow: Flow<UserPreferences> = context.dataStore.data.map { p ->
        UserPreferences(
            sessionEmail = p[Keys.sessionEmail],
            displayName = p[Keys.displayName],
            currencyCode = p[Keys.currencyCode] ?: "USD",
            onboardingComplete = p[Keys.onboardingComplete] ?: false,
            notificationsEnabled = p[Keys.notificationsEnabled] ?: false,
            monthlyBudgetMinor = p[Keys.monthlyBudgetMinor],
            dailyLimitMinor = p[Keys.dailyLimitMinor],
        )
    }

    suspend fun setSession(email: String) {
        context.dataStore.edit { it[Keys.sessionEmail] = email }
    }

    suspend fun clearSession() {
        context.dataStore.edit { it.remove(Keys.sessionEmail) }
    }

    suspend fun setProfile(displayName: String, currencyCode: String) {
        context.dataStore.edit {
            it[Keys.displayName] = displayName
            it[Keys.currencyCode] = currencyCode
        }
    }

    suspend fun setOnboardingComplete(done: Boolean) {
        context.dataStore.edit { it[Keys.onboardingComplete] = done }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.notificationsEnabled] = enabled }
    }

    suspend fun setMonthlyBudgetMinor(amount: Long?) {
        context.dataStore.edit {
            if (amount == null) it.remove(Keys.monthlyBudgetMinor) else it[Keys.monthlyBudgetMinor] = amount
        }
    }

    suspend fun setDailyLimitMinor(amount: Long?) {
        context.dataStore.edit {
            if (amount == null) it.remove(Keys.dailyLimitMinor) else it[Keys.dailyLimitMinor] = amount
        }
    }
}

data class UserPreferences(
    val sessionEmail: String?,
    val displayName: String?,
    val currencyCode: String,
    val onboardingComplete: Boolean,
    val notificationsEnabled: Boolean,
    val monthlyBudgetMinor: Long?,
    val dailyLimitMinor: Long?,
) {
    val isLoggedIn: Boolean get() = !sessionEmail.isNullOrBlank()
}
