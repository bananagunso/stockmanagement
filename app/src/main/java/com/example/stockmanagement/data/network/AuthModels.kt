package com.example.stockmanagement.data.network

import com.google.gson.annotations.SerializedName

data class AuthRequestCodeRequest(
    val email: String
)

data class AuthVerifyCodeRequest(
    val email: String,
    val code: String
)

data class AuthResponse(
    val token: String?,
    val role: String?,
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

data class CreateGroupRequest(
    @SerializedName("display_name") val displayName: String
)

data class CreateGroupResponse(
    val success: Boolean,
    val message: String?,
    val group: InventoryGroupResponse?
)
