package com.example.stockmanagement.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.stockmanagement.data.dao.ItemDao
import com.example.stockmanagement.data.database.AppDatabase

class ItemViewModelFactory(
    private val database: AppDatabase,
    private val itemDao: ItemDao
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {
        if (modelClass.isAssignableFrom(ItemViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ItemViewModel(
                database,
                itemDao
            ) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
