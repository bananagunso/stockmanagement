package com.example.stockmanagement.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stockmanagement.data.dao.DataTypeDao
import com.example.stockmanagement.data.entity.DataTypeEntity
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch

class DataTypeViewModel(
    private val dataTypeDao: DataTypeDao
) : ViewModel() {

    val categories: StateFlow<List<DataTypeEntity>> =
        dataTypeDao.getAll()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
    
    fun addDataType(name: String) {
        viewModelScope.launch {
            dataTypeDao.insert(
                DataTypeEntity(
                    name = name
                )
            )
        }
    }
}
