package com.example.stockmanagement.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stockmanagement.data.dao.AttributeDao
import com.example.stockmanagement.data.dao.DataTypeDao
import com.example.stockmanagement.data.entity.AttributeEntity
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch
import com.example.stockmanagement.data.model.AttributeWithDataType
class AttributeViewModel(
    private val attributeDao: AttributeDao,
    private val dataTypeDao: DataTypeDao,
) : ViewModel() {

    fun addAttribute(name: String, dataTypeId: Int, unit: String?) {
        viewModelScope.launch {
            attributeDao.insert(
                AttributeEntity(
                    name = name,
                    dataTypeId = dataTypeId,
                    unit = unit,
                )
            )
        }
    }

    val attributes: StateFlow<List<AttributeWithDataType>> =
        attributeDao.getAllWithDataType()
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList()
            )

    val dataTypes = dataTypeDao.getAll()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )
}
