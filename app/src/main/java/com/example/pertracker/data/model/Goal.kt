package com.example.pertracker.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "goals")
data class Goal(
    @PrimaryKey(autoGenerate = true)
    val goalId: Long = 0,
    val title: String,
    val targetAmount: Double,
    val currentAmount: Double = 0.0,
    val deadlineDate: Date,
    val isShariaCompliant: Boolean = false
)
