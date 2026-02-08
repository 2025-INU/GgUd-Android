package com.capstone.ggud.network

import com.capstone.ggud.data.TokenStore
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val tokenStore: TokenStore
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val path = original.url.encodedPath

        val skipAuth =
            path == "/api/v1/auth/kakao/login" ||
            path == "/api/v1/auth/refresh"

        if (skipAuth) return chain.proceed(original)

        val accessToken = runBlocking { tokenStore.getAccessToken() }
        val request = if (!accessToken.isNullOrBlank()) {
            original.newBuilder()
                .addHeader("Authorization", "Bearer $accessToken")
                .build()
        } else original

        return chain.proceed(request)
    }
}