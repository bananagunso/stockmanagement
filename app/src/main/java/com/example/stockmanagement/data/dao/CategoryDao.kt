package com.example.stockmanagement.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.stockmanagement.data.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {

    @Query(
        """
        SELECT * FROM category
        WHERE deleted_at IS NULL
        ORDER BY category_id
        """
    )
    fun getAll(): Flow<List<CategoryEntity>>

    @Insert
    suspend fun insert(category: CategoryEntity)

    @Update
    suspend fun update(category: CategoryEntity)

    @Query(
        """
        UPDATE category
        SET
            deleted_at = :deletedAt,
            updated_at = :deletedAt,
            sync_version = sync_version + 1
        WHERE category_id = :categoryId
        AND deleted_at IS NULL
        
    """
    )
    suspend fun softDelete(
        categoryId: Int,
        deletedAt: Long = System.currentTimeMillis()
    )
}
