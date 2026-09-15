package com.example.stockmanagement.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.stockmanagement.data.database.AppDatabase

class DataManagementViewModelFactory(
    private val database: AppDatabase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DataManagementViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DataManagementViewModel(database) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
