package com.example.stockmanagement.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.stockmanagement.data.dao.DatabaseInfoDao
import com.example.stockmanagement.data.entity.DatabaseInfoEntity

@Database(entities = [DatabaseInfoEntity::class], version = 1)
abstract class MasterDatabase : RoomDatabase() {
    abstract fun databaseInfoDao(): DatabaseInfoDao
}
