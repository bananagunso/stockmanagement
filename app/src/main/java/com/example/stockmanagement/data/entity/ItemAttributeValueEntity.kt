package com.example.stockmanagement.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "item_attribute_value",
    foreignKeys = [
        ForeignKey(
            entity = ItemEntity::class,
            parentColumns = ["item_id"],
            childColumns = ["item_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CategoryAttributeEntity::class,
            parentColumns = ["categoryattribute_id"],
            childColumns = ["categoryattribute_id"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["uuid"], unique = true),
        Index("item_id"),
        Index("categoryattribute_id"),
        Index(value = ["item_id", "categoryattribute_id"], unique = true)
    ]
)
data class ItemAttributeValueEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "item_attribute_value_id")
    val itemAttributeValueId: Int = 0,

    @ColumnInfo(name = "item_id")
    val itemId: Int,

    @ColumnInfo(name = "categoryattribute_id")
    val categoryattributeId: Int,

    @ColumnInfo(name = "value")
    val value: String,

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
