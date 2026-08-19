package com.example.stockmanagement.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.stockmanagement.data.dao.AttributeDao
import com.example.stockmanagement.data.dao.CategoryAttributeDao
import com.example.stockmanagement.data.dao.CategoryDao

class CategoryAttributeViewModelFactory(
    private val categoryAttributeDao: CategoryAttributeDao,
    private val categoryDao: CategoryDao,
    private val attributeDao: AttributeDao,
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {

        if (modelClass.isAssignableFrom(CategoryAttributeViewModel::class.java)) {

            @Suppress("UNCHECKED_CAST")
            return CategoryAttributeViewModel(
                categoryAttributeDao = categoryAttributeDao,
                categoryDao = categoryDao,
                attributeDao = attributeDao
                ) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}