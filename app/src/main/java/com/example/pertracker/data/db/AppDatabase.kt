package com.example.pertracker.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.pertracker.data.dao.BudgetDao
import com.example.pertracker.data.dao.CategoryDao
import com.example.pertracker.data.dao.GoalDao
import com.example.pertracker.data.dao.TransactionDao
import com.example.pertracker.data.model.Budget
import com.example.pertracker.data.model.Category
import com.example.pertracker.data.model.Converters
import com.example.pertracker.data.dao.AssetDao
import com.example.pertracker.data.model.AssetEntity
import com.example.pertracker.data.model.Goal
import com.example.pertracker.data.model.TransactionEntity

@Database(
    entities = [
        Category::class,
        TransactionEntity::class,
        Budget::class,
        Goal::class,
        AssetEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao
    abstract fun budgetDao(): BudgetDao
    abstract fun goalDao(): GoalDao
    abstract fun assetDao(): AssetDao
}
