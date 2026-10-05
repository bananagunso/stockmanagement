package com.example.stockmanagement.data.network

import com.google.gson.annotations.SerializedName

data class UserDto(
    val id: Int,
    val email: String,
    val role: String,
    @SerializedName("created_at") val createdAt: Long
)

data class UserListRequest(
    @SerializedName("group_id") val groupId: Int
)

data class UserListResponse(
    val success: Boolean,
    val message: String?,
    val users: List<UserDto> = emptyList()
)

data class UserAddRequest(
    @SerializedName("group_id") val groupId: Int,
    val email: String,
    val role: String
)

data class UserUpdateRoleRequest(
    @SerializedName("group_id") val groupId: Int,
    @SerializedName("user_id") val userId: Int,
    val role: String
)

data class UserDeleteRequest(
    @SerializedName("group_id") val groupId: Int,
    @SerializedName("user_id") val userId: Int
)

data class GenericResponse(
    val success: Boolean,
    val message: String?
)
