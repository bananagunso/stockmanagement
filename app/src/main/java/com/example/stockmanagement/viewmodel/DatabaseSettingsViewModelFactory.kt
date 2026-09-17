package com.example.stockmanagement.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.stockmanagement.data.database.MasterDatabase

class DatabaseSettingsViewModelFactory(
    private val masterDatabase: MasterDatabase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DatabaseSettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DatabaseSettingsViewModel(masterDatabase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
