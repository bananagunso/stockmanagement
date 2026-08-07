package com.example.stockmanagement.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.stockmanagement.data.entity.DataTypeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DataTypeDao {

    @Query("SELECT * FROM data_type ORDER BY data_type_id ")
    fun getAll(): Flow<List<DataTypeEntity>>

    @Insert
    suspend fun insert(datatype: DataTypeEntity)
}
