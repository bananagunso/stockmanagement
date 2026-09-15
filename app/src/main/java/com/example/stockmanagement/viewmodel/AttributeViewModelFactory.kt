package com.example.stockmanagement.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.stockmanagement.data.dao.AttributeDao
import com.example.stockmanagement.data.dao.CategoryAttributeDao
import com.example.stockmanagement.data.dao.DataTypeDao
import com.example.stockmanagement.data.database.AppDatabase

class AttributeViewModelFactory(
    private val database: AppDatabase,
    private val attributeDao: AttributeDao,
    private val dataTypeDao: DataTypeDao,
    private val categoryAttributeDao: CategoryAttributeDao
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {

        if (modelClass.isAssignableFrom(AttributeViewModel::class.java)) {

            @Suppress("UNCHECKED_CAST")
            return AttributeViewModel(
                database = database,
                attributeDao = attributeDao,
                dataTypeDao = dataTypeDao,
                categoryAttributeDao = categoryAttributeDao
            ) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}