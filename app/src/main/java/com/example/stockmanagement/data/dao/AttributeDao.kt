package com.example.stockmanagement.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.stockmanagement.data.entity.AttributeEntity
import com.example.stockmanagement.data.entity.DataTypeEntity
import com.example.stockmanagement.data.model.AttributeWithDataType
import kotlinx.coroutines.flow.Flow

@Dao
interface AttributeDao {

    @Query("SELECT * FROM attribute ORDER BY attribute_id")
    fun getAll(): Flow<List<AttributeEntity>>

    @Insert
    suspend fun insert(attribute: AttributeEntity)

    @Query(
        """
    SELECT
        attribute.attribute_id AS attributeId,
        attribute.name,
        attribute.data_type_id AS dataTypeId,
        data_type.name AS dataTypeName
    FROM attribute
    INNER JOIN data_type
        ON attribute.data_type_id = data_type.data_type_id
"""
    )
    fun getAllWithDataType(): Flow<List<AttributeWithDataType>>

    @Update
    suspend fun update(attribute: AttributeEntity)

    @Query("SELECT * FROM attribute WHERE attribute_id = :id")
    suspend fun getById(id: Int): AttributeEntity
}
