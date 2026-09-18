package com.example.stockmanagement.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "database_info")
data class DatabaseInfoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val displayName: String,
    val fileName: String,
    val isActive: Boolean = false,
    val remoteGroupId: Int? = null // サーバー上のinventory_groups.idと紐付け
)
