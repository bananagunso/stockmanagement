package com.example.stockmanagement.data.dao

import androidx.room.*
import com.example.stockmanagement.data.entity.DatabaseInfoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DatabaseInfoDao {
    @Query("SELECT * FROM database_info ORDER BY id ASC")
    fun getAll(): Flow<List<DatabaseInfoEntity>>

    @Insert
    suspend fun insert(info: DatabaseInfoEntity)

    @Update
    suspend fun update(info: DatabaseInfoEntity)

    @Delete
    suspend fun delete(info: DatabaseInfoEntity)

    @Query("UPDATE database_info SET isActive = (id = :activeId)")
    suspend fun setActive(activeId: Int)

    @Query("SELECT * FROM database_info WHERE isActive = 1 LIMIT 1")
    suspend fun getActive(): DatabaseInfoEntity?
}
