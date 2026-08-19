package com.example.stockmanagement.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.stockmanagement.data.dao.CategoryDao
import com.example.stockmanagement.data.entity.CategoryEntity
import com.example.stockmanagement.data.dao.AttributeDao
import com.example.stockmanagement.data.entity.AttributeEntity
import com.example.stockmanagement.data.dao.CategoryAttributeDao
import com.example.stockmanagement.data.entity.CategoryAttributeEntity
import com.example.stockmanagement.data.dao.DataTypeDao
import com.example.stockmanagement.data.entity.DataTypeEntity

@Database(
    entities = [
        CategoryEntity::class,
        DataTypeEntity::class,
        AttributeEntity::class,
        CategoryAttributeEntity::class,
    ],
    version = 1
)

abstract class AppDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun dataTypeDao(): DataTypeDao
    abstract fun attributeDao(): AttributeDao
    abstract fun categoryAttributeDao(): CategoryAttributeDao
}
