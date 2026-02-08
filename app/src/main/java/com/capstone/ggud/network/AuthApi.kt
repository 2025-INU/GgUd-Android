package com.capstone.ggud.network

import com.capstone.ggud.network.dto.KakaoSdkLoginRequest
import com.capstone.ggud.network.dto.LoginResponse
import com.capstone.ggud.network.dto.TokenRefreshRequest
import com.capstone.ggud.network.dto.TokenRefreshResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {

    @POST("/api/v1/auth/refresh")
    suspend fun refresh(@Body body: TokenRefreshRequest) : TokenRefreshResponse

    @POST("/api/v1/auth/logout")
    suspend fun logout() : Response<Unit>

    @POST("/api/v1/auth/kakao/login")
    suspend fun login(@Body body: KakaoSdkLoginRequest) : LoginResponse
}