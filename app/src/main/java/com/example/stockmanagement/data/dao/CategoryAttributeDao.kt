package com.example.stockmanagement.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.stockmanagement.data.entity.CategoryAttributeEntity
import com.example.stockmanagement.data.model.CategoryWithAttribute
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryAttributeDao {

    @Query("SELECT * FROM categoryattribute ORDER BY categoryattribute_id")
    fun getAll(): Flow<List<CategoryAttributeEntity>>

    @Insert
    suspend fun insert(categoryattribute: CategoryAttributeEntity)

    @Query(
        """
    SELECT
        categoryattribute.categoryattribute_id AS categoryAttributeId,
        category.name AS categoryName,
        attribute.name AS attributeName,
        categoryattribute.unit AS unit
    FROM categoryattribute
    INNER JOIN category
        ON categoryattribute.category_id = category.category_id
    INNER JOIN attribute
        ON categoryattribute.attribute_id = attribute.attribute_id
    ORDER BY categoryattribute.categoryattribute_id
    """
    )
    fun getAllWithAttribute(): Flow<List<CategoryWithAttribute>>

    @Update
    suspend fun update(datatype: CategoryAttributeEntity)
}
