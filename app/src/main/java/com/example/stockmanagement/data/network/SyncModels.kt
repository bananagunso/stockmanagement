package com.example.stockmanagement.data.network

import com.google.gson.annotations.SerializedName

// --- DataType Sync DTO ---
data class DataTypeSyncDto(
    @SerializedName("data_type_id") val dataTypeId: Int,
    val uuid: String,
    val name: String,
    @SerializedName("created_at") val createdAt: Long,
    @SerializedName("updated_at") val updatedAt: Long,
    @SerializedName("deleted_at") val deletedAt: Long?,
    @SerializedName("updated_by_device_id") val updatedByDeviceId: String?,
    @SerializedName("sync_version") val syncVersion: Int
)

// --- Category Sync DTO ---
data class CategorySyncDto(
    val uuid: String,
    val name: String,
    @SerializedName("created_at") val createdAt: Long,
    @SerializedName("updated_at") val updatedAt: Long,
    @SerializedName("deleted_at") val deletedAt: Long?,
    @SerializedName("updated_by_device_id") val updatedByDeviceId: String?,
    @SerializedName("sync_version") val syncVersion: Int
)

// --- Attribute Sync DTO ---
data class AttributeSyncDto(
    val uuid: String,
    val name: String,
    @SerializedName("data_type_id") val dataTypeId: Int,
    @SerializedName("created_at") val createdAt: Long,
    @SerializedName("updated_at") val updatedAt: Long,
    @SerializedName("deleted_at") val deletedAt: Long?,
    @SerializedName("updated_by_device_id") val updatedByDeviceId: String?,
    @SerializedName("sync_version") val syncVersion: Int
)

// --- CategoryAttribute Sync DTO ---
data class CategoryAttributeSyncDto(
    val uuid: String,
    @SerializedName("category_uuid") val categoryUuid: String,
    @SerializedName("attribute_uuid") val attributeUuid: String,
    val unit: String?,
    @SerializedName("created_at") val createdAt: Long,
    @SerializedName("updated_at") val updatedAt: Long,
    @SerializedName("deleted_at") val deletedAt: Long?,
    @SerializedName("updated_by_device_id") val updatedByDeviceId: String?,
    @SerializedName("sync_version") val syncVersion: Int
)

// --- Item Sync DTO ---
data class ItemSyncDto(
    val uuid: String,
    @SerializedName("category_uuid") val categoryUuid: String,
    val name: String,
    val stock: Int,
    @SerializedName("created_at") val createdAt: Long,
    @SerializedName("updated_at") val updatedAt: Long,
    @SerializedName("deleted_at") val deletedAt: Long?,
    @SerializedName("updated_by_device_id") val updatedByDeviceId: String?,
    @SerializedName("sync_version") val syncVersion: Int
)

// --- ItemAttributeValue Sync DTO ---
data class ItemAttributeValueSyncDto(
    val uuid: String,
    @SerializedName("item_uuid") val itemUuid: String,
    @SerializedName("category_attribute_uuid") val categoryAttributeUuid: String,
    val value: String,
    @SerializedName("created_at") val createdAt: Long,
    @SerializedName("updated_at") val updatedAt: Long,
    @SerializedName("deleted_at") val deletedAt: Long?,
    @SerializedName("updated_by_device_id") val updatedByDeviceId: String?,
    @SerializedName("sync_version") val syncVersion: Int
)

// --- Push Request / Response ---
data class SyncPushRequest(
    @SerializedName("group_id") val groupId: Int,
    @SerializedName("device_id") val deviceId: String,
    @SerializedName("data_types") val dataTypes: List<DataTypeSyncDto> = emptyList(),
    val categories: List<CategorySyncDto> = emptyList(),
    val attributes: List<AttributeSyncDto> = emptyList(),
    @SerializedName("category_attributes") val categoryAttributes: List<CategoryAttributeSyncDto> = emptyList(),
    val items: List<ItemSyncDto> = emptyList(),
    @SerializedName("item_attribute_values") val itemAttributeValues: List<ItemAttributeValueSyncDto> = emptyList()
)

data class SyncPushResponse(
    val success: Boolean,
    val message: String?,
    @SerializedName("server_time") val serverTime: Long?,
    @SerializedName("processed_counts") val processedCounts: Map<String, Int>?
)

// --- Pull Request / Response ---
data class SyncPullRequest(
    @SerializedName("group_id") val groupId: Int,
    @SerializedName("device_id") val deviceId: String,
    @SerializedName("last_synced_at") val lastSyncedAt: Long
)

data class SyncPullResponse(
    val success: Boolean,
    @SerializedName("server_time") val serverTime: Long?,
    @SerializedName("data_types") val dataTypes: List<DataTypeSyncDto> = emptyList(),
    val categories: List<CategorySyncDto> = emptyList(),
    val attributes: List<AttributeSyncDto> = emptyList(),
    @SerializedName("category_attributes") val categoryAttributes: List<CategoryAttributeSyncDto> = emptyList(),
    val items: List<ItemSyncDto> = emptyList(),
    @SerializedName("item_attribute_values") val itemAttributeValues: List<ItemAttributeValueSyncDto> = emptyList()
)
