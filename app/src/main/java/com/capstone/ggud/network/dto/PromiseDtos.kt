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
    val createdAt: String?,
    val participants: List<ParticipantResponse>
)

enum class PromiseStatus {
    CREATED,
    RECRUITING,
    SELECTING_MIDPOINT,
    MIDPOINT_CONFIRMED,
    PLACE_CONFIRMED,
    IN_PROGRESS,
    COMPLETED,
    CANCELED
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
    val participantCount: Int,
    val host: Boolean
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
    val host: Boolean,
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

data class DirectionResponse(
    val routeOptions: List<RouteOption>
)

data class RouteOption(
    val totalDuration: Int,
    val totalDistance: Int,
    val totalFare: Int,
    val transferCount: Int,
    val routes: List<RouteStep>
)

data class RouteStep(
    val type: RouteStepType,
    val instruction: String,
    val duration: Int,
    val distance: Int,
    val lineName: String?,
    val linestring: String?
)

enum class RouteStepType{
    WALK, BUS, SUBWAY, TRANSFER
}

data class MapDataResponse(
    val promiseId: Long,
    val destination: MapMarker?,
    val participantDepartures: List<ParticipantMarker>,
    val recommendedMidpoints: List<MapMarker>,
    val currentLocations: List<ParticipantMarker>
)

data class MapMarker(
    val latitude: Double,
    val longitude: Double,
    val name: String,
    val type: String
)

data class ParticipantMarker(
    val userId: Long,
    val nickname: String,
    val profileImageUrl: String?,
    val latitude: Double,
    val longitude: Double,
    val host: Boolean
)

data class PromiseArrivalResponse(
    val participants: List<PromiseArrivalParticipant>,
    val totalCount: Int,
    val arrivedCount: Int
)

data class PromiseArrivalParticipant(
    val userId: Long,
    val nickname: String,
    val arrived: Boolean,
    val arrivedAt: String?
)

data class LocationSendRequest(
    val latitude: Double,
    val longitude: Double
)

data class LocationSocketResponse(
    val userId: Long,
    val nickname: String,
    val profileImageUrl: String?,
    val latitude: Double,
    val longitude: Double,
    val timestamp: String
)

data class UpdateMyExpenseRequest(
    val amount: Long
)

data class SettlementResponse(
    val promiseId: Long,
    val promiseName: String,
    val totalAmount: Long,
    val perPersonAmount: Long,
    val participantCount: Int,
    val settlementCompletedAt: String?,
    val expenses: List<ExpenseRecordResponse>,
    val transfers: List<SettlementTransferResponse>,
    val settlementCompleted: Boolean
)

data class ExpenseRecordResponse(
    val userId: Long,
    val nickname: String,
    val profileImageUrl: String?,
    val paidAmount: Long,
    val balanceAmount: Long,
    val status: String
)

data class SettlementTransferResponse(
    val fromUserId: Long,
    val fromNickname: String,
    val toUserId: Long,
    val toNickname: String,
    val amount: Long
)