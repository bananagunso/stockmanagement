package com.example.stockmanagement.data.network

data class AuthRequestCodeRequest(
    val email: String
)

data class AuthVerifyCodeRequest(
    val email: String,
    val code: String
)

data class AuthResponse(
    val token: String?,
    val message: String?,
    val success: Boolean
)

data class InventoryGroupResponse(
    val id: Int,
    val display_name: String,
    val role: String
)

data class GroupListResponse(
    val success: Boolean,
    val groups: List<InventoryGroupResponse>
)
