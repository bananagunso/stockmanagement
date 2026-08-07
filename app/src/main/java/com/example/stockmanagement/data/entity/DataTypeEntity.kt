package com.example.stockmanagement.data.entity

import androidx.room.ColumnInfo
import androidx.room.Index
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "data_type",
    indices = [
        Index(value = ["uuid"], unique = true),
        Index("deleted_at")
    ]
)
data class DataTypeEntity(

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "data_type_id")
    val dataTypeId: Int = 0,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "deleted_at")
    val deletedAt: Long? = null,

    @ColumnInfo(name = "updated_by_device_id")
    val updatedByDeviceId: String = "",

    @ColumnInfo(name = "sync_version")
    val syncVersion: Int = 0,

    @ColumnInfo(name = "uuid")
    val uuid: String = UUID.randomUUID().toString()
)
