package com.capstone.ggud.data

import com.capstone.ggud.network.UserApi
import com.capstone.ggud.network.dto.UpdateProfileRequest
import com.capstone.ggud.network.dto.UserResponse

class UserRepository(
    private val userApi: UserApi
) {
    suspend fun getMyPage(): UserResponse {
        return userApi.getMyPage()
    }

    suspend fun updateMy(
        nickname: String,
        profileImageUrl: String?
    ): UserResponse {
        val request = UpdateProfileRequest(
            nickname = nickname,
            profileImageUrl = profileImageUrl
        )
        return userApi.updateMy(request)
    }
}