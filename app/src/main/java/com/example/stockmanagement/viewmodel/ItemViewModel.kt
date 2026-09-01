package com.example.stockmanagement.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.withTransaction
import com.example.stockmanagement.data.dao.ItemDao
import com.example.stockmanagement.data.database.AppDatabase
import com.example.stockmanagement.data.entity.ItemEntity
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch

class ItemViewModel(
    private val database: AppDatabase,
    private val itemDao: ItemDao,
) : ViewModel() {

    val items: StateFlow<List<ItemEntity>> =
        itemDao.getAll()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    fun addItem(name: String, stock: Int, categoryId: Int) {
        viewModelScope.launch {
            itemDao.insert(
                ItemEntity(
                    name = name,
                    stock = stock,
                    categoryId = categoryId
                )
            )
        }
    }

    fun editItem(item: ItemEntity, newName: String) {
        viewModelScope.launch {
            itemDao.update(
                item.copy(
                    name = newName,
                    updatedAt = System.currentTimeMillis(),
                    syncVersion = item.syncVersion + 1
                )
            )
        }
    }

    fun deleteItem(itemId: Int) {
        viewModelScope.launch {
            database.withTransaction {
                val now = System.currentTimeMillis()

                itemDao.softDelete(
                    itemId,
                    now
                )
            }
        }
    }
}
