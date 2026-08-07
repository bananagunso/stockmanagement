package com.example.stockmanagement.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.stockmanagement.data.dao.CategoryDao

class CategoryViewModelFactory(
    private val categoryDao: CategoryDao
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {
        if (modelClass.isAssignableFrom(CategoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CategoryViewModel(categoryDao) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}