package com.example.stockmanagement.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.stockmanagement.data.dao.CategoryAttributeDao
import com.example.stockmanagement.data.dao.CategoryDao
import com.example.stockmanagement.data.database.AppDatabase

class CategoryViewModelFactory(
    private val database: AppDatabase,
    private val categoryDao: CategoryDao,
    private val categoryAttributeDao: CategoryAttributeDao
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {
        if (modelClass.isAssignableFrom(CategoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CategoryViewModel(
                database,
                categoryDao,
                categoryAttributeDao
            ) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}