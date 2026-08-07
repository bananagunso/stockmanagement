package com.example.stockmanagement.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.stockmanagement.data.dao.AttributeDao
import com.example.stockmanagement.data.dao.DataTypeDao

class AttributeViewModelFactory(
    private val attributeDao: AttributeDao,
    private val dataTypeDao: DataTypeDao
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {

        if (modelClass.isAssignableFrom(AttributeViewModel::class.java)) {

            @Suppress("UNCHECKED_CAST")
            return AttributeViewModel(
                attributeDao,
                dataTypeDao
            ) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}