package com.example.stockmanagement.data.model

data class CategoryWithAttribute(
    val categoryAttributeId: Int,
    val categoryName: String,
    val attributeName: String,
    val unit: String?,
)