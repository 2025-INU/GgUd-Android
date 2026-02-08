package com.capstone.ggud.data

import com.capstone.ggud.network.AuthApi
import com.capstone.ggud.network.dto.KakaoSdkLoginRequest
import com.capstone.ggud.network.dto.LoginResponse
import com.capstone.ggud.network.dto.TokenRefreshRequest

class AuthRepository(
    private val api: AuthApi,
    private val tokenStore: TokenStore
) {

    suspend fun loginWithKakaoSdk(kakaoAccessToken: String): LoginResponse {
        val res = api.login(
            KakaoSdkLoginRequest(kakaoAccessToken = kakaoAccessToken)
        )
        tokenStore.saveLogin(res)
        return res
    }

    suspend fun refreshIfNeedded(thresholdMs: Long = 60_000L): String? {
        val currentAccess = tokenStore.getAccessToken()
        val expiresAt = tokenStore.getExpiresAt()

        if (expiresAt == null) return currentAccess

        val remainMs = expiresAt - System.currentTimeMillis()
        if (remainMs > thresholdMs) return currentAccess

        val refresh = tokenStore.getRefreshToken() ?: return null
        val refreshed = api.refresh(TokenRefreshRequest(refreshToken = refresh))

        val merged = LoginResponse(
            accessToken = refreshed.accessToken,
            refreshToken = refresh,
            tokenType = refreshed.tokenType,
            expiresIn = refreshed.expiresIn,
            userId = null,
            nickname = null
        )
        tokenStore.saveLogin(merged)

        return refreshed.accessToken
    }

    suspend fun logout(): Boolean {
        val res = runCatching { api.logout() }.getOrNull()
        tokenStore.clearAll()

        return res?.isSuccessful == true
    }
}