package com.example.stockmanagement.data.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface ApiService {
    @POST("api/auth/request-code.php")
    suspend fun requestCode(@Body request: AuthRequestCodeRequest): Response<AuthResponse>

    @POST("api/auth/verify-code.php")
    suspend fun verifyCode(@Body request: AuthVerifyCodeRequest): Response<AuthResponse>
    
    @GET("api/groups/list.php")
    suspend fun getGroups(@Header("Authorization") token: String): Response<GroupListResponse>

    @POST("api/groups/create.php")
    suspend fun createGroup(
        @Header("Authorization") token: String,
        @Body request: CreateGroupRequest
    ): Response<CreateGroupResponse>
    
    @POST("api/sync/push.php")
    suspend fun pushSync(
        @Header("Authorization") token: String,
        @Body request: SyncPushRequest
    ): Response<SyncPushResponse>

    @POST("api/sync/pull.php")
    suspend fun pullSync(
        @Header("Authorization") token: String,
        @Body request: SyncPullRequest
    ): Response<SyncPullResponse>

    // --- User Management APIs ---
    @POST("api/users/list.php")
    suspend fun getUsers(
        @Header("Authorization") token: String,
        @Body request: UserListRequest
    ): Response<UserListResponse>

    @POST("api/users/add.php")
    suspend fun addUser(
        @Header("Authorization") token: String,
        @Body request: UserAddRequest
    ): Response<GenericResponse>

    @POST("api/users/update_role.php")
    suspend fun updateUserRole(
        @Header("Authorization") token: String,
        @Body request: UserUpdateRoleRequest
    ): Response<GenericResponse>

    @POST("api/users/delete.php")
    suspend fun deleteUser(
        @Header("Authorization") token: String,
        @Body request: UserDeleteRequest
    ): Response<GenericResponse>
}
