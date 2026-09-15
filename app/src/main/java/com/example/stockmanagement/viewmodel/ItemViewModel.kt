package com.example.stockmanagement.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.withTransaction
import com.example.stockmanagement.data.dao.ItemDao
import com.example.stockmanagement.data.database.AppDatabase
import com.example.stockmanagement.data.entity.ItemEntity
import com.example.stockmanagement.data.model.ItemWithCategory
import com.example.stockmanagement.data.model.AttributeValueDetail
import com.example.stockmanagement.data.model.ItemWithDetails
import com.example.stockmanagement.util.StringUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ItemViewModel(
    private val database: AppDatabase,
    private val itemDao: ItemDao,
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _selectedCategoryId = MutableStateFlow<Int?>(null)
    val selectedCategoryId: StateFlow<Int?> = _selectedCategoryId

    val items: StateFlow<List<ItemEntity>> =
        itemDao.getAll()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    private val allItemWithCategory: StateFlow<List<ItemWithCategory>> =
        itemDao.getAllWithCategory()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    private val allAttributeValues = database.itemAttributeValueDao().getAllValues()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val allCategoryAttributes = database.categoryAttributeDao().getAllWithAttribute()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val itemWithCategory: StateFlow<List<ItemWithDetails>> =
        combine(
            allItemWithCategory,
            allAttributeValues,
            allCategoryAttributes,
            _searchQuery,
            _selectedCategoryId
        ) { items, attrValues, catAttrs, query, categoryId ->
            val normalizedQuery = StringUtil.toHalfWidth(query)

            items.map { item ->
                val detailsForItem = attrValues
                    .filter { it.itemId == item.itemId }
                    .mapNotNull { valEntity ->
                        val catAttr = catAttrs.find { it.categoryAttributeId == valEntity.categoryattributeId }
                        catAttr?.let {
                            AttributeValueDetail(
                                attributeName = it.attributeName,
                                value = valEntity.value,
                                unit = it.unit
                            )
                        }
                    }

                ItemWithDetails(
                    itemId = item.itemId,
                    itemName = item.itemName,
                    stock = item.stock,
                    categoryId = item.categoryId,
                    categoryName = item.categoryName,
                    attributeDetails = detailsForItem
                )
            }.filter { item ->
                val normalizedItemName = StringUtil.toHalfWidth(item.itemName)
                val normalizedCategoryName = StringUtil.toHalfWidth(item.categoryName)
                val normalizedAttrValues = item.attributeDetails.map { StringUtil.toHalfWidth(it.value) }

                val matchesQuery = normalizedItemName.contains(normalizedQuery, ignoreCase = true) ||
                        normalizedCategoryName.contains(normalizedQuery, ignoreCase = true) ||
                        normalizedAttrValues.any { it.contains(normalizedQuery, ignoreCase = true) }

                val matchesCategory = categoryId == null || item.categoryId == categoryId
                matchesQuery && matchesCategory
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setCategoryFilter(categoryId: Int?) {
        _selectedCategoryId.value = categoryId
    }

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

    fun editItem(itemId: Int, newName: String, newCategoryId: Int, newStock: Int) {
        viewModelScope.launch {
            val item = itemDao.getById(itemId)

            itemDao.update(
                item.copy(
                    name = newName,
                    categoryId = newCategoryId,
                    stock = newStock,
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
