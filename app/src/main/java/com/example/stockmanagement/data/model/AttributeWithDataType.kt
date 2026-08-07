package com.example.stockmanagement.data.model

data class AttributeWithDataType(
    val attributeId: Int,
    val name: String,
    val dataTypeId: Int,
    val unit: String?,
    val dataTypeName: String
)