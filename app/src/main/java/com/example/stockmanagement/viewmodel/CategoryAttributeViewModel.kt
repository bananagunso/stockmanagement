package com.example.stockmanagement.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stockmanagement.data.dao.CategoryAttributeDao
import com.example.stockmanagement.data.dao.CategoryDao
import com.example.stockmanagement.data.dao.AttributeDao
import com.example.stockmanagement.data.entity.CategoryAttributeEntity
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch
import com.example.stockmanagement.data.model.CategoryWithAttribute

class CategoryAttributeViewModel(
    private val categoryAttributeDao: CategoryAttributeDao,
    categoryDao: CategoryDao,
    attributeDao: AttributeDao,
) : ViewModel() {

    fun addCategoryAttribute(categoryId: Int, attributeId: Int, unit: String?) {
        viewModelScope.launch {
            categoryAttributeDao.insert(
                CategoryAttributeEntity(
                    categoryId = categoryId,
                    attributeId = attributeId,
                    unit = unit
                )
            )
        }
    }

    val categoryAttributes: StateFlow<List<CategoryWithAttribute>> =
        categoryAttributeDao.getAllWithAttribute()
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList()
            )

    val categories = categoryDao.getAll()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    val attributes = attributeDao.getAll()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )
}
