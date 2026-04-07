package com.example.pertracker.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class CategoryType {
    INCOME, EXPENSE, TRANSFER_GOAL
}

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true)
    val categoryId: Long = 0,
    val name: String,
    val type: CategoryType,
    val isSystemDefault: Boolean = false
)
