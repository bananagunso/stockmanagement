package com.example.stockmanagement.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.stockmanagement.data.dao.CategoryDao
import com.example.stockmanagement.data.entity.CategoryEntity

@Database(
    entities = [
        CategoryEntity::class
    ],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
}
