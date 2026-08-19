package com.example.stockmanagement.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "categoryattribute",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["category_id"],
            childColumns = ["category_id"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = AttributeEntity::class,
            parentColumns = ["attribute_id"],
            childColumns = ["attribute_id"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["uuid"], unique = true),
        Index("deleted_at"),
        Index("category_id"),
        Index("attribute_id"),
        Index(
            value = ["category_id", "attribute_id"],
            unique = true
        )
    ]
)

data class CategoryAttributeEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "categoryattribute_id")
    val categoryattributeId: Int = 0,

    @ColumnInfo(name = "category_id")
    val categoryId: Int = 0,

    @ColumnInfo(name = "attribute_id")
    val attributeId: Int = 0,

    @ColumnInfo(name = "unit")
    val unit: String? = null,

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
