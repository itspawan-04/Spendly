package com.example.spendly.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.spendly.SpendlyApplication
import com.example.spendly.data.db.CategoryEntity
import com.example.spendly.data.db.ExpenseEntity
import com.example.spendly.data.preferences.UserPreferences
import com.example.spendly.data.repository.SpendlyRepository
import com.example.spendly.data.repository.toYyyyMmInt
import com.example.spendly.data.repository.toYyyyMmInt
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SpendlyViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: SpendlyRepository =
        (application as SpendlyApplication).repository

    private val _mainTabIndex = MutableStateFlow(0)
    val mainTabIndex: StateFlow<Int> = _mainTabIndex.asStateFlow()

    fun selectMainTab(index: Int) {
        _mainTabIndex.value = index
    }

    val userPreferences: StateFlow<UserPreferences> = repository.userPreferences
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            UserPreferences(null, null, "USD", false, false, null, null),
        )

    val categories = repository.observeCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val recentExpenses = repository.observeRecentExpenses()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun signIn(email: String, password: String, onResult: (String?) -> Unit) {
        viewModelScope.launch {
            if (!email.contains('@')) {
                onResult("Enter a valid email")
                return@launch
            }
            if (password.length < 8) {
                onResult("Password must be at least 8 characters")
                return@launch
            }
            repository.setSession(email.trim())
            onResult(null)
        }
    }

    fun signUp(email: String, password: String, onResult: (String?) -> Unit) {
        signIn(email, password, onResult)
    }

    fun signOut() {
        viewModelScope.launch { repository.clearSession() }
    }

    fun continueLocal() {
        viewModelScope.launch { repository.setSession("local@spendly.app") }
    }

    fun completeOnboarding(
        displayName: String,
        currencyCode: String,
        monthlyBudgetMinor: Long?,
        notificationsEnabled: Boolean,
    ) {
        viewModelScope.launch {
            repository.setProfile(displayName.ifBlank { "Friend" }, currencyCode)
            repository.setMonthlyBudgetMinor(monthlyBudgetMinor)
            repository.setNotificationsEnabled(notificationsEnabled)
            repository.setOnboardingComplete(true)
        }
    }

    fun skipOnboarding() {
        viewModelScope.launch {
            repository.setProfile("Friend", "USD")
            repository.setOnboardingComplete(true)
        }
    }

    fun addExpense(
        title: String,
        amountMinor: Long,
        categoryId: Long,
        date: LocalDate,
        notes: String,
        recurrence: String?,
        onResult: (String?) -> Unit,
    ) {
        viewModelScope.launch {
            if (title.isBlank()) {
                onResult("Title is required")
                return@launch
            }
            if (!repository.validateExpenseDate(date)) {
                onResult("Date must be within 5 years past or future")
                return@launch
            }
            repository.insertExpense(title, amountMinor, categoryId, date, notes, recurrence)
            onResult(null)
        }
    }

    fun updateExpense(entity: ExpenseEntity, onResult: (String?) -> Unit) {
        viewModelScope.launch {
            repository.updateExpense(entity)
            onResult(null)
        }
    }

    fun deleteExpense(id: Long) {
        viewModelScope.launch { repository.softDeleteExpense(id) }
    }

    fun setMonthlyBudget(minor: Long?) {
        viewModelScope.launch { repository.setMonthlyBudgetMinor(minor) }
    }

    fun setDailyLimit(minor: Long?) {
        viewModelScope.launch { repository.setDailyLimitMinor(minor) }
    }

    fun setCategoryBudget(categoryId: Long, ym: YearMonth, amountMinor: Long, rollover: Boolean) {
        viewModelScope.launch {
            repository.setCategoryBudget(categoryId, ym.toYyyyMmInt(), amountMinor, rollover)
        }
    }

    suspend fun monthTotal(ym: YearMonth): Long = repository.sumMonth(ym)

    suspend fun previousMonthTotal(ym: YearMonth): Long = repository.sumMonthPrevious(ym)

    suspend fun categoryBreakdown(ym: YearMonth): List<Pair<CategoryEntity, Long>> =
        repository.categoryBreakdown(ym)

    suspend fun trendMonths(): List<Pair<YearMonth, Long>> = repository.trendLastMonths(6)

    suspend fun topDays(ym: YearMonth): List<Pair<LocalDate, Long>> = repository.topSpendingDays(ym)

    suspend fun dayOfWeekTotals(ym: YearMonth) = repository.dayOfWeekTotals(ym)

    suspend fun dailyTotals(ym: YearMonth) = repository.dailyTotalsForMonth(ym)

    fun observeExpensesForDay(epochDay: Long) = repository.observeExpensesForDay(epochDay)

    fun observeExpensesBetween(startDay: Long, endDay: Long) =
        repository.observeExpensesBetween(startDay, endDay)

    fun observeCategoryBudgets(ym: YearMonth) = repository.observeCategoryBudgets(ym.toYyyyMmInt())

    suspend fun getExpense(id: Long) = repository.getExpense(id)

    suspend fun getCategory(id: Long) = repository.getCategory(id)

    suspend fun expensesForExport(start: LocalDate, end: LocalDate): List<ExpenseEntity> =
        repository.expensesBetween(start, end)

    suspend fun allCategoriesMap(): Map<Long, CategoryEntity> =
        repository.observeAllCategories().first().associateBy { it.id }

    fun addCategory(name: String, emoji: String, colorArgb: Long, onResult: (String?) -> Unit) {
        viewModelScope.launch {
            if (name.isBlank()) {
                onResult("Name required")
                return@launch
            }
            repository.addCategory(name, emoji, colorArgb)
            onResult(null)
        }
    }

    fun archiveCategory(id: Long) {
        viewModelScope.launch { repository.archiveCategory(id) }
    }
}
