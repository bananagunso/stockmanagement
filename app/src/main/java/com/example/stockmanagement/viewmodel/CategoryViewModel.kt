package com.example.stockmanagement.ui.viewmodel

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
                    name = name,
                    categoryId = TODO(),
                    createdAt = TODO(),
                    updatedAt = TODO(),
                    deletedAt = TODO(),
                    updatedByDeviceId = TODO(),
                    syncVersion = TODO(),
                    uuid = TODO()
                )
            )
        }
    }
}
