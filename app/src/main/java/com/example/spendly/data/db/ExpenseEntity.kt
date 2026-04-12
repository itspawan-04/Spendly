package com.example.spendly.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(
    tableName = "expenses",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.RESTRICT,
        ),
    ],
    indices = [Index("categoryId"), Index("dateEpochDay"), Index("deletedAtMillis")],
)
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    /** Amount stored in minor units (e.g. cents) for precision */
    val amountMinor: Long,
    val categoryId: Long,
    val dateEpochDay: Long,
    val notes: String = "",
    val recurrence: String? = null,
    val createdAtMillis: Long = System.currentTimeMillis(),
    val deletedAtMillis: Long? = null,
) {
    val date: LocalDate get() = LocalDate.ofEpochDay(dateEpochDay)
}
