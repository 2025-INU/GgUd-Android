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
    val status: PromiseStatus,
    val inviteCode: String?,
    val inviteExpiredAt: String?,
    val maxParticipants: Int?,
    val hostId: Long?,
    val hostNickname: String?,
    val participantCount: Int,
    val confirmedLatitude: Double?,
    val confirmedLongitude: Double?,
    val confirmedPlaceName: String?,
    val createdAt: String?
)

enum class PromiseStatus {
    CREATED,
    RECRUITING,
    WAITING_LOCATIONS,
    SELECTING_MIDPOINT,
    CONFIRMED,
    IN_PROGRESS,
    COMPLETED
}

//GET /api/v1/promises
data class PagePromiseResponse(
    val totalElements: Long,
    val totalPages: Int,
    val first: Boolean,
    val size: Int,
    val content: List<PromiseResponse>,
    val number: Int,
    val sort: SortObject?,
    val numberOfElements: Int,
    val pageable: PageableObject?,
    val last: Boolean,
    val empty: Boolean
)

data class SortObject(
    val empty: Boolean,
    val sorted: Boolean,
    val unsorted: Boolean
)

data class PageableObject(
    val offset: Long,
    val sort: SortObject?,
    val paged: Boolean,
    val pageNumber: Int,
    val pageSize: Int,
    val unpaged: Boolean
)