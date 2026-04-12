package com.example.spendly.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryBudgetDao {
    @Query("SELECT * FROM category_budgets WHERE yearMonth = :yearMonth")
    fun observeForMonth(yearMonth: Int): Flow<List<CategoryBudgetEntity>>

    @Query("SELECT * FROM category_budgets WHERE yearMonth = :yearMonth AND categoryId = :categoryId LIMIT 1")
    suspend fun getForCategoryMonth(categoryId: Long, yearMonth: Int): CategoryBudgetEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: CategoryBudgetEntity)

    @Query("DELETE FROM category_budgets WHERE id = :id")
    suspend fun deleteById(id: Long)
}
