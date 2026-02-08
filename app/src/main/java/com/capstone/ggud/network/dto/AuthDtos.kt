package com.capstone.ggud.network.dto

//POST /api/v1/auth/refresh
data class TokenRefreshRequest(
    val refreshToken: String
)
data class TokenRefreshResponse(
    val accessToken: String,
    val tokenType: String = "Bearer",
    val expiresIn: Long = 3600
)

//POST /api/v1/auth/kakao/login
data class KakaoSdkLoginRequest(
    val kakaoAccessToken: String
)
data class LoginResponse(
    val accessToken: String,
    val refreshToken: String? = null,
    val tokenType: String = "Bearer",
    val expiresIn: Long = 3600,
    val userId: Long? = null,
    val nickname: String? = null
)