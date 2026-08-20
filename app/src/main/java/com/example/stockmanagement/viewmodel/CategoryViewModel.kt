package com.example.stockmanagement.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stockmanagement.data.dao.CategoryDao
import com.example.stockmanagement.data.entity.CategoryEntity
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch

class CategoryViewModel(
    private val categoryDao: CategoryDao
) : ViewModel() {

    val categories: StateFlow<List<CategoryEntity>> =
        categoryDao.getAll()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
    
    fun addCategory(name: String) {
        viewModelScope.launch {
            categoryDao.insert(
                CategoryEntity(
                    name = name
                )
            )
        }
    }

    fun editCategory(category: CategoryEntity, newName: String) {
        viewModelScope.launch {
            categoryDao.update(
                category.copy(
                    name = newName,
                    updatedAt = System.currentTimeMillis(),
                    syncVersion = category.syncVersion + 1
                )
            )
        }
    }
}
