package com.capstone.ggud.network.dto

//GET /api/v1/users/me
data class UserResponse(
    val id: Long,
    val nickname: String,
    val profileImageUrl: String?,
    val email: String
)

//PATCH /api/v1/users/me
data class UpdateProfileRequest(
    val nickname: String,
    val profileImageUrl: String? = null
)