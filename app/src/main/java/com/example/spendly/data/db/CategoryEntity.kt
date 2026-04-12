package com.example.spendly.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "categories",
    indices = [Index(value = ["sortOrder"])]
)
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val iconEmoji: String,
    val colorArgb: Long,
    val sortOrder: Int,
    val isArchived: Boolean = false,
    val isSystemDefault: Boolean = false,
)
