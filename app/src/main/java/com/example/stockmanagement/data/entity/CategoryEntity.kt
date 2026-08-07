package com.example.stockmanagement.data.entity

import androidx.room.ColumnInfo
import androidx.room.Index
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "category",
    indices = [
        Index(value = ["uuid"], unique = true),
        Index("deleted_at")
    ]
)
data class CategoryEntity(

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "category_id")
    val categoryId: Int = 0,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "created_at")
    val createdAt: Long,

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long,

    @ColumnInfo(name = "deleted_at")
    val deletedAt: Long? = null,

    @ColumnInfo(name = "updated_by_device_id")
    val updatedByDeviceId: String? = null,

    @ColumnInfo(name = "sync_version")
    val syncVersion: Int = 0,

    @ColumnInfo(name = "uuid")
    val uuid: String,
)
