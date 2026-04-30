package com.capstone.ggud.network.dto

import com.google.gson.annotations.SerializedName

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
    MIDPOINT_CONFIRMED,
    PLACE_CONFIRMED,
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

data class PromiseSummaryResponse(
    val id: Long,
    val title: String,
    val promiseDateTime: String,
    val hostId: Long,
    val hostNickname: String
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

data class InviteLinkResponse(
    val inviteCode: String,
    val inviteUrl: String,
    val expiredAt: String,
    val isValid: Boolean
)

data class ParticipantResponse(
    val id: Long,
    val userId: Long,
    val nickname: String,
    val profileImageUrl: String?,
    val departureLatitude: Double?,
    val departureLongitude: Double?,
    val departureAddress: String?,
    val locationSubmitted: Boolean,
    val host: Boolean,
    val joinedAt: String
)

data class UpdateDepartureRequest(
    val latitude: Double,
    val longitude: Double,
    val address: String? = null
)

data class Coordinate(
    val latitude: Double,
    val longitude: Double
)

data class MidpointRecommendationResponse(
    val calculatedMidpoint: Coordinate,
    val recommendedStations: List<StationRecommendation>,
    val participantCount: Int
)

data class ParticipantTravelInfo(
    val userId: Long,
    val nickname: String,
    val departureAddress: String,
    val travelTimeMinutes: Int,
    val distanceMeters: Int
)

data class StationRecommendation(
    val stationId: Long,
    val stationName: String,
    val lineName: String,
    val latitude: Double,
    val longitude: Double,
    val distanceFromMidpoint: Double,
    val averageDistanceFromParticipants: Double,
    val participantTravelInfos: List<ParticipantTravelInfo>,
    val averageTravelTimeMinutes: Int
)

data class InviteCodeResponse(
    val inviteCode: String,
    val expiredAt: String,
    val isValid: Boolean
)

data class PlaceRecommendationRequest(
    val query: String,
    val tab: PlaceRecommendationTab
)

enum class PlaceRecommendationTab {
    ALL,
    RESTAURANT,
    CAFE,
    BAR
}

data class PlaceRecommendationResponse(
    val recommendations: List<PlaceRecommendationItem>,
    @SerializedName("promise_id")
    val promiseId: Long
)

data class PlaceRecommendationItem(
    val category: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    @SerializedName("place_id")
    val placeId: String,
    @SerializedName("place_name")
    val placeName: String,
    @SerializedName("image_url")
    val imageUrl: String,
    @SerializedName("ai_summary")
    val aiSummary: String,
    @SerializedName("ai_score")
    val aiScore: Double,
    @SerializedName("distance_from_midpoint")
    val distanceFromMidpoint: Double
)

data class ConfirmMidpointRequest(
    val stationId: Long
)

data class PlaceConfirmRequest(
    val placeId: String,
    val placeName: String,
    val latitude: Double,
    val longitude: Double
)