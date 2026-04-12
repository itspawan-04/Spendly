package com.example.spendly.data.repository

import com.example.spendly.data.db.AppDatabase
import com.example.spendly.data.db.CategoryBudgetDao
import com.example.spendly.data.db.CategoryBudgetEntity
import com.example.spendly.data.db.CategoryDao
import com.example.spendly.data.db.CategoryEntity
import com.example.spendly.data.db.CategorySpend
import com.example.spendly.data.db.DaySpend
import com.example.spendly.data.db.ExpenseDao
import com.example.spendly.data.db.ExpenseEntity
import com.example.spendly.data.preferences.UserPreferencesRepository
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class SpendlyRepository(
    private val db: AppDatabase,
    private val userPreferencesRepository: UserPreferencesRepository,
) {
    private val categoryDao: CategoryDao = db.categoryDao()
    private val expenseDao: ExpenseDao = db.expenseDao()
    private val budgetDao: CategoryBudgetDao = db.categoryBudgetDao()

    val userPreferences = userPreferencesRepository.preferencesFlow

    suspend fun ensureDefaultData() {
        if (categoryDao.count() == 0) {
            val defaults = listOf(
                Triple("Food", "🍔", 0xFFE11D48L),
                Triple("Transport", "🚗", 0xFF2563EBL),
                Triple("Housing", "🏠", 0xFF7C3AEDL),
                Triple("Health", "🏥", 0xFF059669L),
                Triple("Leisure", "🎉", 0xFFEA580CL),
                Triple("Shopping", "🛍", 0xFFDB2777L),
                Triple("Utilities", "💡", 0xFFCA8A04L),
                Triple("Education", "📚", 0xFF4F46E5L),
                Triple("Travel", "✈️", 0xFF0891B2L),
                Triple("Savings", "💰", 0xFF16A34AL),
                Triple("Subscriptions", "📱", 0xFF6366F1L),
                Triple("Other", "🔧", 0xFF64748BL),
            )
            defaults.forEachIndexed { index, (name, emoji, color) ->
                categoryDao.insert(
                    CategoryEntity(
                        name = name,
                        iconEmoji = emoji,
                        colorArgb = color and 0xFFFFFFFFL,
                        sortOrder = index,
                        isSystemDefault = true,
                    ),
                )
            }
        }
    }

    fun observeCategories(): Flow<List<CategoryEntity>> = categoryDao.observeActive()

    fun observeAllCategories(): Flow<List<CategoryEntity>> = categoryDao.observeAll()

    fun observeRecentExpenses(): Flow<List<ExpenseEntity>> = expenseDao.observeRecent(10)

    fun observeExpensesForDay(epochDay: Long): Flow<List<ExpenseEntity>> =
        expenseDao.observeForDay(epochDay)

    fun observeExpensesBetween(startDay: Long, endDay: Long): Flow<List<ExpenseEntity>> =
        expenseDao.observeBetweenDays(startDay, endDay)

    suspend fun getExpense(id: Long): ExpenseEntity? = expenseDao.getById(id)

    suspend fun getCategory(id: Long): CategoryEntity? = categoryDao.getById(id)

    suspend fun insertExpense(
        title: String,
        amountMinor: Long,
        categoryId: Long,
        date: LocalDate,
        notes: String,
        recurrence: String?,
    ): Long {
        val entity = ExpenseEntity(
            title = title,
            amountMinor = amountMinor,
            categoryId = categoryId,
            dateEpochDay = date.toEpochDay(),
            notes = notes,
            recurrence = recurrence,
        )
        return expenseDao.insert(entity)
    }

    suspend fun updateExpense(entity: ExpenseEntity) {
        expenseDao.update(entity)
    }

    suspend fun softDeleteExpense(id: Long) {
        expenseDao.softDelete(id)
    }

    suspend fun addCategory(name: String, iconEmoji: String, colorArgb: Long) {
        val order = categoryDao.nextSortOrder()
        categoryDao.insert(
            CategoryEntity(
                name = name,
                iconEmoji = iconEmoji,
                colorArgb = colorArgb and 0xFFFFFFFFL,
                sortOrder = order,
            ),
        )
    }

    suspend fun updateCategory(entity: CategoryEntity) {
        categoryDao.update(entity)
    }

    suspend fun archiveCategory(id: Long) {
        categoryDao.archive(id)
    }

    suspend fun monthBounds(ym: YearMonth): Pair<Long, Long> {
        val start = ym.atDay(1)
        val end = ym.atEndOfMonth()
        return start.toEpochDay() to end.toEpochDay()
    }

    suspend fun sumMonth(ym: YearMonth): Long {
        val (s, e) = monthBounds(ym)
        return expenseDao.sumBetweenDays(s, e)
    }

    suspend fun sumMonthPrevious(ym: YearMonth): Long {
        val prev = ym.minusMonths(1)
        return sumMonth(prev)
    }

    suspend fun categoryBreakdown(ym: YearMonth): List<Pair<CategoryEntity, Long>> {
        val (s, e) = monthBounds(ym)
        val spends: List<CategorySpend> = expenseDao.sumByCategoryBetween(s, e)
        val categories = categoryDao.observeAll().first().associateBy { it.id }
        return spends.mapNotNull { cs ->
            categories[cs.categoryId]?.let { it to cs.total }
        }.sortedByDescending { it.second }
    }

    suspend fun dailyTotalsForMonth(ym: YearMonth): Map<Long, Long> {
        val (s, e) = monthBounds(ym)
        return expenseDao.sumByDayBetween(s, e).associate { it.dateEpochDay to it.total }
    }

    suspend fun trendLastMonths(count: Int = 6): List<Pair<YearMonth, Long>> {
        val now = YearMonth.now()
        return (count - 1 downTo 0).map { offset ->
            val ym = now.minusMonths(offset.toLong())
            ym to sumMonth(ym)
        }
    }

    suspend fun topSpendingDays(ym: YearMonth, take: Int = 5): List<Pair<LocalDate, Long>> {
        val map = dailyTotalsForMonth(ym).entries.sortedByDescending { it.value }.take(take)
        return map.map { (day, total) -> LocalDate.ofEpochDay(day) to total }
    }

    suspend fun dayOfWeekTotals(ym: YearMonth): Map<DayOfWeek, Long> {
        val (s, e) = monthBounds(ym)
        val expenses = expenseDao.observeBetweenDays(s, e).first()
        val result = DayOfWeek.entries.associateWith { 0L }.toMutableMap()
        for (ex in expenses) {
            val dow = ex.date.dayOfWeek
            result[dow] = (result[dow] ?: 0L) + ex.amountMinor
        }
        return result
    }

    suspend fun setCategoryBudget(categoryId: Long, yearMonth: Int, amountMinor: Long, rollover: Boolean) {
        budgetDao.upsert(
            CategoryBudgetEntity(
                categoryId = categoryId,
                yearMonth = yearMonth,
                amountMinor = amountMinor,
                rollover = rollover,
            ),
        )
    }

    fun observeCategoryBudgets(yearMonth: Int): Flow<List<CategoryBudgetEntity>> =
        budgetDao.observeForMonth(yearMonth)

    suspend fun expensesBetween(start: LocalDate, end: LocalDate): List<ExpenseEntity> {
        val s = start.toEpochDay()
        val e = end.toEpochDay()
        return expenseDao.observeBetweenDays(s, e).first()
    }

    suspend fun setSession(email: String) = userPreferencesRepository.setSession(email)
    suspend fun clearSession() = userPreferencesRepository.clearSession()
    suspend fun setProfile(name: String, currency: String) =
        userPreferencesRepository.setProfile(name, currency)
    suspend fun setOnboardingComplete(done: Boolean) =
        userPreferencesRepository.setOnboardingComplete(done)
    suspend fun setNotificationsEnabled(enabled: Boolean) =
        userPreferencesRepository.setNotificationsEnabled(enabled)
    suspend fun setMonthlyBudgetMinor(v: Long?) = userPreferencesRepository.setMonthlyBudgetMinor(v)
    suspend fun setDailyLimitMinor(v: Long?) = userPreferencesRepository.setDailyLimitMinor(v)

    suspend fun validateExpenseDate(date: LocalDate): Boolean {
        val today = LocalDate.now()
        val min = today.minusYears(5)
        val max = today.plusYears(5)
        return !date.isBefore(min) && !date.isAfter(max)
    }
}

fun YearMonth.toYyyyMmInt(): Int = year * 100 + monthValue
