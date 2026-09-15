package com.example.stockmanagement.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.stockmanagement.data.dao.ItemAttributeValueDao
import com.example.stockmanagement.data.dao.CategoryAttributeDao

class ItemAttributeValueViewModelFactory(
    private val itemAttributeValueDao: ItemAttributeValueDao,
    private val categoryAttributeDao: CategoryAttributeDao
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ItemAttributeValueViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ItemAttributeValueViewModel(
                itemAttributeValueDao,
                categoryAttributeDao
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
