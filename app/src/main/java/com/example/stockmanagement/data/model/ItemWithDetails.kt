package com.example.stockmanagement.data.model

data class ItemWithDetails(
    val itemId: Int,
    val itemName: String,
    val stock: Int,
    val categoryId: Int,
    val categoryName: String,
    val attributeDetails: List<AttributeValueDetail> = emptyList()
)

data class AttributeValueDetail(
    val attributeName: String,
    val value: String,
    val unit: String?
)
