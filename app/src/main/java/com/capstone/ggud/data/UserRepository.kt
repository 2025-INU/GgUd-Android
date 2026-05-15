package com.capstone.ggud.data

import com.capstone.ggud.network.UserApi
import com.capstone.ggud.network.dto.UpdateProfileRequest
import com.capstone.ggud.network.dto.UserResponse
import okhttp3.MultipartBody

class UserRepository(
    private val api: UserApi
) {
    suspend fun getMyPage(): UserResponse {
        return api.getMyPage()
    }

    suspend fun updateMy(
        nickname: String,
        profileImageUrl: String?
    ): UserResponse {
        val request = UpdateProfileRequest(
            nickname = nickname,
            profileImageUrl = profileImageUrl
        )
        return api.updateMy(request)
    }

    suspend fun uploadProfileImage(image: MultipartBody.Part): UserResponse {
        return api.uploadProfileImage(image)
    }
}