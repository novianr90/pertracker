package com.example.pertracker.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(
    tableName = "budgets",
    foreignKeys = [
        ForeignKey(
            entity = Category::class,
            parentColumns = ["categoryId"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["categoryId", "month", "year"], unique = true)]
)
data class Budget(
    @PrimaryKey(autoGenerate = true)
    val budgetId: Long = 0,
    val categoryId: Long,
    val month: Int,
    val year: Int,
    val plannedAmount: Double,
    val currentAmount: Double = 0.0
)
