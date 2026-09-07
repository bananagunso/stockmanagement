package com.example.stockmanagement.data.model

data class ItemWithCategory(
    val itemId: Int,
    val itemName: String,
    val stock: Int,
    val categoryName: String
)