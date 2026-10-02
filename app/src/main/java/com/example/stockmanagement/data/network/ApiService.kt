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
}
