package com.capstone.ggud.network.dto

//POST /api/v1/promises
data class CreatePromiseRequest(
    val title: String,
    val description: String = "",
    val promiseDateTime: String
)
data class PromiseResponse(
    val id: Long,
    val title: String,
    val description: String?,
    val promiseDateTime: String,
    val status: String,
    val inviteCode: String?,
    val inviteExpiredAt: String?,
    val maxParticipants: Int?,
    val hostId: Long?,
    val hostNickname: String?,
    val confirmedLatitude: Double?,
    val confirmedLongitude: Double?,
    val confirmedPlaceName: String?,
    val createAt: String?
)