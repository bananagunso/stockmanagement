package com.example.stockmanagement.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.stockmanagement.data.entity.ItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {

    @Query("SELECT * FROM item ORDER BY item_id ")
    fun getAll(): Flow<List<ItemEntity>>

    @Insert
    suspend fun insert(dataType: ItemEntity)

    @Update
    suspend fun update(dataType: ItemEntity)

    @Query(
        """
        UPDATE item
        SET
            deleted_at = :deletedAt,
            updated_at = :deletedAt,
            sync_version = sync_version + 1
        WHERE item_id = :itemId
        AND deleted_at IS NULL
        
    """
    )
    suspend fun softDelete(
        itemId: Int,
        deletedAt: Long = System.currentTimeMillis()
    )
}
