package com.capstone.ggud.network

import com.capstone.ggud.network.dto.UpdateProfileRequest
import com.capstone.ggud.network.dto.UserResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH

interface UserApi {

    @GET("/api/v1/users/me")
    suspend fun getMyPage(): UserResponse

    @PATCH("/api/v1/users/me")
    suspend fun updateMy(
        @Body request: UpdateProfileRequest
    ): UserResponse
}