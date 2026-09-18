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
    
    // 今後、ここに同期(sync)用APIを追加していきます
}
