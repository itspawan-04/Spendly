package com.example.spendly.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Query(
        """
        SELECT e.* FROM expenses e
        WHERE e.deletedAtMillis IS NULL
        ORDER BY e.dateEpochDay DESC, e.id DESC
        LIMIT :limit
        """
    )
    fun observeRecent(limit: Int = 10): Flow<List<ExpenseEntity>>

    @Query(
        """
        SELECT e.* FROM expenses e
        WHERE e.deletedAtMillis IS NULL AND e.dateEpochDay = :epochDay
        ORDER BY e.id DESC
        """
    )
    fun observeForDay(epochDay: Long): Flow<List<ExpenseEntity>>

    @Query(
        """
        SELECT e.* FROM expenses e
        WHERE e.deletedAtMillis IS NULL
        AND e.dateEpochDay BETWEEN :startDay AND :endDay
        ORDER BY e.dateEpochDay DESC, e.id DESC
        """
    )
    fun observeBetweenDays(startDay: Long, endDay: Long): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): ExpenseEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: ExpenseEntity): Long

    @Update
    suspend fun update(entity: ExpenseEntity)

    @Query("UPDATE expenses SET deletedAtMillis = :now WHERE id = :id")
    suspend fun softDelete(id: Long, now: Long = System.currentTimeMillis())

    @Query(
        """
        SELECT COALESCE(SUM(amountMinor), 0) FROM expenses
        WHERE deletedAtMillis IS NULL AND dateEpochDay BETWEEN :startDay AND :endDay
        """
    )
    suspend fun sumBetweenDays(startDay: Long, endDay: Long): Long

    @Query(
        """
        SELECT categoryId, COALESCE(SUM(amountMinor), 0) AS total
        FROM expenses
        WHERE deletedAtMillis IS NULL AND dateEpochDay BETWEEN :startDay AND :endDay
        GROUP BY categoryId
        """
    )
    suspend fun sumByCategoryBetween(startDay: Long, endDay: Long): List<CategorySpend>

    @Query(
        """
        SELECT dateEpochDay, COALESCE(SUM(amountMinor), 0) AS total
        FROM expenses
        WHERE deletedAtMillis IS NULL AND dateEpochDay BETWEEN :startDay AND :endDay
        GROUP BY dateEpochDay
        """
    )
    suspend fun sumByDayBetween(startDay: Long, endDay: Long): List<DaySpend>
}

data class CategorySpend(val categoryId: Long, val total: Long)

data class DaySpend(val dateEpochDay: Long, val total: Long)
