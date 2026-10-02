package com.example.stockmanagement.data.repository

import android.content.Context
import android.provider.Settings
import androidx.room.withTransaction
import com.example.stockmanagement.data.database.AppDatabase
import com.example.stockmanagement.data.entity.AttributeEntity
import com.example.stockmanagement.data.entity.CategoryAttributeEntity
import com.example.stockmanagement.data.entity.CategoryEntity
import com.example.stockmanagement.data.entity.DataTypeEntity
import com.example.stockmanagement.data.entity.ItemAttributeValueEntity
import com.example.stockmanagement.data.entity.ItemEntity
import com.example.stockmanagement.data.network.ApiService
import com.example.stockmanagement.data.network.AttributeSyncDto
import com.example.stockmanagement.data.network.CategoryAttributeSyncDto
import com.example.stockmanagement.data.network.CategorySyncDto
import com.example.stockmanagement.data.network.DataTypeSyncDto
import com.example.stockmanagement.data.network.ItemAttributeValueSyncDto
import com.example.stockmanagement.data.network.ItemSyncDto
import com.example.stockmanagement.data.network.SyncPullRequest
import com.example.stockmanagement.data.network.SyncPushRequest
import com.example.stockmanagement.util.TokenManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.util.Locale
import java.util.UUID

class SyncRepository(
    private val context: Context,
    private val database: AppDatabase,
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) {
    private val prefs = context.getSharedPreferences("sync_prefs", Context.MODE_PRIVATE)

    private val deviceId: String
        get() {
            var id = prefs.getString("device_id", null)
            if (id == null) {
                id = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
                    ?: UUID.randomUUID().toString()
                prefs.edit().putString("device_id", id).apply()
            }
            return id
        }

    fun getLastSyncedAt(groupId: Int): Long {
        return prefs.getLong("last_synced_at_$groupId", 0L)
    }

    private fun setLastSyncedAt(groupId: Int, timestamp: Long) {
        prefs.edit().putLong("last_synced_at_$groupId", timestamp).apply()
    }

    suspend fun sync(groupId: Int = 1): Result<String> = withContext(Dispatchers.IO) {
        try {
            val token = tokenManager.getToken()
                ?: return@withContext Result.failure(Exception("ログインしていません"))

            val authHeader = "Bearer $token"
            val lastSyncedAt = getLastSyncedAt(groupId)

            // --- Step 1: Push (ローカル変更の送信) ---
            val modifiedDataTypes = database.dataTypeDao().getModifiedSince(lastSyncedAt)
            val modifiedCategories = database.categoryDao().getModifiedSince(lastSyncedAt)
            val modifiedAttributes = database.attributeDao().getModifiedSince(lastSyncedAt)
            val modifiedCategoryAttributes = database.categoryAttributeDao().getModifiedSince(lastSyncedAt)
            val modifiedItems = database.itemDao().getModifiedSince(lastSyncedAt)
            val modifiedItemValues = database.itemAttributeValueDao().getModifiedSince(lastSyncedAt)

            val dataTypeDtos = modifiedDataTypes.map {
                DataTypeSyncDto(
                    dataTypeId = it.dataTypeId,
                    uuid = it.uuid,
                    name = it.name,
                    createdAt = it.createdAt,
                    updatedAt = it.updatedAt,
                    deletedAt = it.deletedAt,
                    updatedByDeviceId = it.updatedByDeviceId.ifEmpty { deviceId },
                    syncVersion = it.syncVersion
                )
            }

            val categoryDtos = modifiedCategories.map {
                CategorySyncDto(
                    uuid = it.uuid,
                    name = it.name,
                    createdAt = it.createdAt,
                    updatedAt = it.updatedAt,
                    deletedAt = it.deletedAt,
                    updatedByDeviceId = it.updatedByDeviceId.ifEmpty { deviceId },
                    syncVersion = it.syncVersion
                )
            }

            val attributeDtos = modifiedAttributes.map {
                AttributeSyncDto(
                    uuid = it.uuid,
                    name = it.name,
                    dataTypeId = it.dataTypeId,
                    createdAt = it.createdAt,
                    updatedAt = it.updatedAt,
                    deletedAt = it.deletedAt,
                    updatedByDeviceId = it.updatedByDeviceId.ifEmpty { deviceId },
                    syncVersion = it.syncVersion
                )
            }

            val categoryAttributeDtos = modifiedCategoryAttributes.mapNotNull { ca ->
                val category = database.categoryDao().getAll().first().find { it.categoryId == ca.categoryId }
                    ?: database.categoryDao().getModifiedSince(0).find { it.categoryId == ca.categoryId }
                val attribute = database.attributeDao().getById(ca.attributeId)

                if (category != null) {
                    CategoryAttributeSyncDto(
                        uuid = ca.uuid,
                        categoryUuid = category.uuid,
                        attributeUuid = attribute.uuid,
                        unit = ca.unit,
                        createdAt = ca.createdAt,
                        updatedAt = ca.updatedAt,
                        deletedAt = ca.deletedAt,
                        updatedByDeviceId = ca.updatedByDeviceId.ifEmpty { deviceId },
                        syncVersion = ca.syncVersion
                    )
                } else null
            }

            val itemDtos = modifiedItems.mapNotNull { item ->
                val category = database.categoryDao().getAll().first().find { it.categoryId == item.categoryId }
                    ?: database.categoryDao().getModifiedSince(0).find { it.categoryId == item.categoryId }

                if (category != null) {
                    ItemSyncDto(
                        uuid = item.uuid,
                        categoryUuid = category.uuid,
                        name = item.name,
                        stock = item.stock,
                        createdAt = item.createdAt,
                        updatedAt = item.updatedAt,
                        deletedAt = item.deletedAt,
                        updatedByDeviceId = item.updatedByDeviceId ?: deviceId,
                        syncVersion = item.syncVersion
                    )
                } else null
            }

            val itemValueDtos = modifiedItemValues.mapNotNull { valEntity ->
                val item = database.itemDao().getById(valEntity.itemId)
                val catAttr = database.categoryAttributeDao().getById(valEntity.categoryattributeId)

                if (item != null && catAttr != null) {
                    ItemAttributeValueSyncDto(
                        uuid = valEntity.uuid,
                        itemUuid = item.uuid,
                        categoryAttributeUuid = catAttr.uuid,
                        value = valEntity.value,
                        createdAt = valEntity.createdAt,
                        updatedAt = valEntity.updatedAt,
                        deletedAt = valEntity.deletedAt,
                        updatedByDeviceId = valEntity.updatedByDeviceId.ifEmpty { deviceId },
                        syncVersion = valEntity.syncVersion
                    )
                } else null
            }

            val hasLocalChanges = dataTypeDtos.isNotEmpty() || categoryDtos.isNotEmpty() ||
                    attributeDtos.isNotEmpty() || categoryAttributeDtos.isNotEmpty() ||
                    itemDtos.isNotEmpty() || itemValueDtos.isNotEmpty()

            var pushServerTime = lastSyncedAt

            if (hasLocalChanges) {
                val pushRequest = SyncPushRequest(
                    groupId = groupId,
                    deviceId = deviceId,
                    dataTypes = dataTypeDtos,
                    categories = categoryDtos,
                    attributes = attributeDtos,
                    categoryAttributes = categoryAttributeDtos,
                    items = itemDtos,
                    itemAttributeValues = itemValueDtos
                )

                val pushResponse = apiService.pushSync(authHeader, pushRequest)
                if (pushResponse.isSuccessful && pushResponse.body()?.success == true) {
                    pushServerTime = pushResponse.body()?.serverTime ?: System.currentTimeMillis()
                } else {
                    return@withContext Result.failure(
                        Exception(pushResponse.body()?.message ?: "Push同期に失敗しました")
                    )
                }
            }

            // --- Step 2: Pull (リモート更新の受信とローカル反映) ---
            val pullRequest = SyncPullRequest(
                groupId = groupId,
                deviceId = deviceId,
                lastSyncedAt = lastSyncedAt
            )

            val pullResponse = apiService.pullSync(authHeader, pullRequest)
            if (!pullResponse.isSuccessful || pullResponse.body()?.success != true) {
                return@withContext Result.failure(Exception("Pull同期に失敗しました"))
            }

            val remoteData = pullResponse.body()!!
            val newSyncedAt = remoteData.serverTime ?: pushServerTime

            // トランザクション処理で正しい依存関係順序で取り込み
            database.withTransaction {
                // 1. Data Types (依存の最上位のため最初に同期)
                remoteData.dataTypes.forEach { dto ->
                    val existing = database.dataTypeDao().getByUuid(dto.uuid)
                        ?: database.dataTypeDao().getById(dto.dataTypeId)
                    if (existing != null) {
                        database.dataTypeDao().update(
                            existing.copy(
                                name = dto.name,
                                updatedAt = dto.updatedAt,
                                deletedAt = dto.deletedAt,
                                updatedByDeviceId = dto.updatedByDeviceId ?: "",
                                syncVersion = dto.syncVersion
                            )
                        )
                    } else {
                        database.dataTypeDao().insert(
                            DataTypeEntity(
                                dataTypeId = dto.dataTypeId,
                                uuid = dto.uuid,
                                name = dto.name,
                                createdAt = dto.createdAt,
                                updatedAt = dto.updatedAt,
                                deletedAt = dto.deletedAt,
                                updatedByDeviceId = dto.updatedByDeviceId ?: "",
                                syncVersion = dto.syncVersion
                            )
                        )
                    }
                }

                // 2. Categories
                remoteData.categories.forEach { dto ->
                    val existing = database.categoryDao().getByUuid(dto.uuid)
                    if (existing != null) {
                        database.categoryDao().update(
                            existing.copy(
                                name = dto.name,
                                updatedAt = dto.updatedAt,
                                deletedAt = dto.deletedAt,
                                updatedByDeviceId = dto.updatedByDeviceId ?: "",
                                syncVersion = dto.syncVersion
                            )
                        )
                    } else {
                        database.categoryDao().insert(
                            CategoryEntity(
                                uuid = dto.uuid,
                                name = dto.name,
                                createdAt = dto.createdAt,
                                updatedAt = dto.updatedAt,
                                deletedAt = dto.deletedAt,
                                updatedByDeviceId = dto.updatedByDeviceId ?: "",
                                syncVersion = dto.syncVersion
                            )
                        )
                    }
                }

                // 3. Attributes
                remoteData.attributes.forEach { dto ->
                    val existingDataType = database.dataTypeDao().getById(dto.dataTypeId)
                    if (existingDataType == null) {
                        database.dataTypeDao().insert(
                            DataTypeEntity(
                                dataTypeId = dto.dataTypeId,
                                uuid = "00000000-0000-0000-0000-0000000000" + String.format(Locale.US, "%02d", dto.dataTypeId),
                                name = "データ型 ${dto.dataTypeId}"
                            )
                        )
                    }

                    val existing = database.attributeDao().getByUuid(dto.uuid)
                    if (existing != null) {
                        database.attributeDao().update(
                            existing.copy(
                                name = dto.name,
                                dataTypeId = dto.dataTypeId,
                                updatedAt = dto.updatedAt,
                                deletedAt = dto.deletedAt,
                                updatedByDeviceId = dto.updatedByDeviceId ?: "",
                                syncVersion = dto.syncVersion
                            )
                        )
                    } else {
                        database.attributeDao().insert(
                            AttributeEntity(
                                uuid = dto.uuid,
                                name = dto.name,
                                dataTypeId = dto.dataTypeId,
                                createdAt = dto.createdAt,
                                updatedAt = dto.updatedAt,
                                deletedAt = dto.deletedAt,
                                updatedByDeviceId = dto.updatedByDeviceId ?: "",
                                syncVersion = dto.syncVersion
                            )
                        )
                    }
                }

                // 4. CategoryAttributes
                remoteData.categoryAttributes.forEach { dto ->
                    val category = database.categoryDao().getByUuid(dto.categoryUuid)
                    val attribute = database.attributeDao().getByUuid(dto.attributeUuid)

                    if (category != null && attribute != null) {
                        val existing = database.categoryAttributeDao().getByUuid(dto.uuid)
                        if (existing != null) {
                            database.categoryAttributeDao().update(
                                existing.copy(
                                    categoryId = category.categoryId,
                                    attributeId = attribute.attributeId,
                                    unit = dto.unit,
                                    updatedAt = dto.updatedAt,
                                    deletedAt = dto.deletedAt,
                                    updatedByDeviceId = dto.updatedByDeviceId ?: "",
                                    syncVersion = dto.syncVersion
                                )
                            )
                        } else {
                            database.categoryAttributeDao().insert(
                                CategoryAttributeEntity(
                                    uuid = dto.uuid,
                                    categoryId = category.categoryId,
                                    attributeId = attribute.attributeId,
                                    unit = dto.unit,
                                    createdAt = dto.createdAt,
                                    updatedAt = dto.updatedAt,
                                    deletedAt = dto.deletedAt,
                                    updatedByDeviceId = dto.updatedByDeviceId ?: "",
                                    syncVersion = dto.syncVersion
                                )
                            )
                        }
                    }
                }

                // 5. Items
                remoteData.items.forEach { dto ->
                    val category = database.categoryDao().getByUuid(dto.categoryUuid)
                    if (category != null) {
                        val existing = database.itemDao().getByUuid(dto.uuid)
                        if (existing != null) {
                            database.itemDao().update(
                                existing.copy(
                                    categoryId = category.categoryId,
                                    name = dto.name,
                                    stock = dto.stock,
                                    updatedAt = dto.updatedAt,
                                    deletedAt = dto.deletedAt,
                                    updatedByDeviceId = dto.updatedByDeviceId,
                                    syncVersion = dto.syncVersion
                                )
                            )
                        } else {
                            database.itemDao().insert(
                                ItemEntity(
                                    uuid = dto.uuid,
                                    categoryId = category.categoryId,
                                    name = dto.name,
                                    stock = dto.stock,
                                    createdAt = dto.createdAt,
                                    updatedAt = dto.updatedAt,
                                    deletedAt = dto.deletedAt,
                                    updatedByDeviceId = dto.updatedByDeviceId,
                                    syncVersion = dto.syncVersion
                                )
                            )
                        }
                    }
                }

                // 6. ItemAttributeValues
                remoteData.itemAttributeValues.forEach { dto ->
                    val item = database.itemDao().getByUuid(dto.itemUuid)
                    val catAttr = database.categoryAttributeDao().getByUuid(dto.categoryAttributeUuid)

                    if (item != null && catAttr != null) {
                        val existing = database.itemAttributeValueDao().getByUuid(dto.uuid)
                        if (existing != null) {
                            database.itemAttributeValueDao().update(
                                existing.copy(
                                    itemId = item.itemId,
                                    categoryattributeId = catAttr.categoryattributeId,
                                    value = dto.value,
                                    updatedAt = dto.updatedAt,
                                    deletedAt = dto.deletedAt,
                                    updatedByDeviceId = dto.updatedByDeviceId ?: "",
                                    syncVersion = dto.syncVersion
                                )
                            )
                        } else {
                            database.itemAttributeValueDao().insert(
                                ItemAttributeValueEntity(
                                    uuid = dto.uuid,
                                    itemId = item.itemId,
                                    categoryattributeId = catAttr.categoryattributeId,
                                    value = dto.value,
                                    createdAt = dto.createdAt,
                                    updatedAt = dto.updatedAt,
                                    deletedAt = dto.deletedAt,
                                    updatedByDeviceId = dto.updatedByDeviceId ?: "",
                                    syncVersion = dto.syncVersion
                                )
                            )
                        }
                    }
                }
            }

            setLastSyncedAt(groupId, newSyncedAt)
            Result.success("同期が成功しました")
        } catch (e: Exception) {
            e.printStackTrace()
            val msg = if (e.message?.contains("JsonReader") == true || e.message?.contains("malformed") == true) {
                "サーバー応答エラー: JSON以外のレスポンス（404 Not Found や PHPエラー画面）が返されました。PHPファイルの配置またはPostgreSQLテーブル作成をご確認ください。"
            } else {
                e.message ?: "同期中にエラーが発生しました"
            }
            Result.failure(Exception(msg))
        }
    }
}
