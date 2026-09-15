package com.example.stockmanagement.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.stockmanagement.data.entity.ItemAttributeValueEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemAttributeValueDao {
    @Query("SELECT * FROM item_attribute_value WHERE item_id = :itemId AND deleted_at IS NULL")
    fun getValuesByItem(itemId: Int): Flow<List<ItemAttributeValueEntity>>

    @Insert
    suspend fun insert(value: ItemAttributeValueEntity)

    @Update
    suspend fun update(value: ItemAttributeValueEntity)

    @Query("SELECT * FROM item_attribute_value WHERE deleted_at IS NULL")
    fun getAllValues(): Flow<List<ItemAttributeValueEntity>>

    @Query("SELECT * FROM item_attribute_value WHERE item_id = :itemId AND categoryattribute_id = :categoryAttributeId AND deleted_at IS NULL LIMIT 1")
    suspend fun getByItemAndCategoryAttribute(itemId: Int, categoryAttributeId: Int): ItemAttributeValueEntity?
}
