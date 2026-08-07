package com.example.stockmanagement.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.stockmanagement.data.dao.DataTypeDao

class DataTypeViewModelFactory(
    private val dataTypeDao: DataTypeDao
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {
        if (modelClass.isAssignableFrom(DataTypeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DataTypeViewModel(dataTypeDao) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}