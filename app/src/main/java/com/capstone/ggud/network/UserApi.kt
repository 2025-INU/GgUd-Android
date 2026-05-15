package com.capstone.ggud.network

import com.capstone.ggud.network.dto.UpdateProfileRequest
import com.capstone.ggud.network.dto.UserResponse
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part

interface UserApi {

    @GET("/api/v1/users/me")
    suspend fun getMyPage(): UserResponse

    @PATCH("/api/v1/users/me")
    suspend fun updateMy(
        @Body request: UpdateProfileRequest
    ): UserResponse

    @Multipart
    @POST("/api/v1/users/me/profile-image")
    suspend fun uploadProfileImage(
        @Part image: MultipartBody.Part
    ): UserResponse
}