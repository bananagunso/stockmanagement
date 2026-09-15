package com.example.stockmanagement.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stockmanagement.data.dao.ItemAttributeValueDao
import com.example.stockmanagement.data.dao.CategoryAttributeDao
import com.example.stockmanagement.data.entity.ItemAttributeValueEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ItemAttributeValueViewModel(
    private val itemAttributeValueDao: ItemAttributeValueDao,
    private val categoryAttributeDao: CategoryAttributeDao
) : ViewModel() {

    // 特定のアイテムに関連する属性値を保存する
    fun saveAttributeValue(itemId: Int, categoryAttributeId: Int, value: String) {
        viewModelScope.launch {
            val existing = itemAttributeValueDao.getByItemAndCategoryAttribute(itemId, categoryAttributeId)
            if (existing != null) {
                itemAttributeValueDao.update(
                    existing.copy(
                        value = value,
                        updatedAt = System.currentTimeMillis(),
                        syncVersion = existing.syncVersion + 1
                    )
                )
            } else {
                itemAttributeValueDao.insert(
                    ItemAttributeValueEntity(
                        itemId = itemId,
                        categoryattributeId = categoryAttributeId,
                        value = value
                    )
                )
            }
        }
    }

    // アイテムに関連する現在の値を取得するフロー（詳細はScreen側で結合）
    fun getItemValues(itemId: Int) = itemAttributeValueDao.getValuesByItem(itemId)
}
