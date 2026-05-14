package com.capstone.ggud.data

import com.capstone.ggud.network.PromiseApi
import com.capstone.ggud.network.dto.ConfirmMidpointRequest
import com.capstone.ggud.network.dto.CreatePromiseRequest
import com.capstone.ggud.network.dto.DirectionResponse
import com.capstone.ggud.network.dto.MapDataResponse
import com.capstone.ggud.network.dto.MidpointRecommendationResponse
import com.capstone.ggud.network.dto.PagePromiseResponse
import com.capstone.ggud.network.dto.ParticipantResponse
import com.capstone.ggud.network.dto.PlaceConfirmRequest
import com.capstone.ggud.network.dto.PlaceRecommendationRequest
import com.capstone.ggud.network.dto.PlaceRecommendationResponse
import com.capstone.ggud.network.dto.PlaceRecommendationTab
import com.capstone.ggud.network.dto.PromiseArrivalResponse
import com.capstone.ggud.network.dto.PromiseResponse
import com.capstone.ggud.network.dto.PromiseStatus
import com.capstone.ggud.network.dto.PromiseSummaryResponse
import com.capstone.ggud.network.dto.SettlementResponse
import com.capstone.ggud.network.dto.UpdateDepartureRequest
import com.capstone.ggud.network.dto.UpdateMyExpenseRequest

class PromiseRepository(
    private val api: PromiseApi
) {
    suspend fun createPromise(title: String, promiseDateTimeIso: String): PromiseResponse {
        val req = CreatePromiseRequest(
            title = title.trim(),
            description = "",
            promiseDateTime = promiseDateTimeIso
        )
        return api.createPromise(req)
    }

    suspend fun getMyPromises(
        status: PromiseStatus? = null,
        keyword: String? = null,
        page: Int = 0,
        size: Int = 20
    ): PagePromiseResponse {
        return api.getMyPromises(
            status = status,
            keyword = keyword,
            page = page,
            size = size
        )
    }

    suspend fun getPromiseParticipants(promiseId: Long): List<ParticipantResponse> {
        return api.getPromiseParticipants(promiseId)
    }

    suspend fun getPromiseSummary(promiseId: Long): PromiseSummaryResponse {
        return api.getPromiseSummary(promiseId)
    }

    suspend fun updateDeparture(
        promiseId: Long,
        latitude: Double,
        longitude: Double,
        address: String?
    ) {
        api.updateDeparture(
            promiseId = promiseId,
            body = UpdateDepartureRequest(
                latitude = latitude,
                longitude = longitude,
                address = address
            )
        )
    }

    suspend fun startMidpointSelection(promiseId: Long) {
        api.startMidpointSelection(promiseId)
    }

    suspend fun getMidpointRecommendations(
        promiseId: Long
    ): MidpointRecommendationResponse {
        return api.getMidpointRecommendations(promiseId)
    }

    suspend fun getInviteCode(promiseId: Long): String {
        return api.getInviteCode(promiseId).inviteCode
    }

    suspend fun getPromiseByInviteCode(inviteCode: String): PromiseResponse {
        return api.getPromiseByInviteCode(inviteCode.trim())
    }

    suspend fun getPlaceRecommendations(
        promiseId: Long,
        query: String = "",
        tab: PlaceRecommendationTab = PlaceRecommendationTab.ALL
    ): PlaceRecommendationResponse {
        return api.getPlaceRecommendations(
            promiseId = promiseId,
            body = PlaceRecommendationRequest(
                query = query,
                tab = tab
            )
        )
    }

    suspend fun confirmMidpoint(
        promiseId: Long,
        stationId: Long
    ): Result<Unit> {
        return runCatching {
            val response = api.confirmMidpoint(
                promiseId = promiseId,
                request = ConfirmMidpointRequest(stationId)
            )

            if (!response.isSuccessful) {
                throw Exception("중간지점 확정 실패")
            }
        }
    }

    suspend fun confirmPlace(
        promiseId: Long,
        placeId: String,
        placeName: String,
        latitude: Double,
        longitude: Double
    ) {
        api.confirmPlace(
            promiseId = promiseId,
            body = PlaceConfirmRequest(
                placeId = placeId,
                placeName = placeName,
                latitude = latitude,
                longitude = longitude
            )
        )
    }

    suspend fun joinPromiseByInviteCode(inviteCode: String): PromiseResponse {
        return api.joinPromiseByInviteCode(inviteCode.trim())
    }

    suspend fun resetMidpoint(promiseId: Long) {
        api.resetMidpoint(promiseId)
    }

    suspend fun getDirections(
        promiseId: Long,
        originLat: Double,
        originLon: Double,
        destLat: Double,
        destLon: Double
    ): DirectionResponse {
        return api.getDirections(
            promiseId = promiseId,
            originLat = originLat,
            originLon = originLon,
            destLat = destLat,
            destLon = destLon
        )
    }

    suspend fun getMapData(promiseId: Long): MapDataResponse {
        return api.getMapData(promiseId)
    }

    suspend fun getPromiseArrivals(promiseId: Long): PromiseArrivalResponse {
        return api.getPromiseArrivals(promiseId)
    }

    suspend fun completePromise(promiseId: Long) {
        val response = api.completePromise(promiseId)

        if (!response.isSuccessful) {
            throw Exception("약속 종료 실패: ${response.code()}")
        }
    }

    suspend fun getExpenses(promiseId: Long): SettlementResponse {
        return api.getExpenses(promiseId)
    }

    suspend fun updateMyExpense(
        promiseId: Long,
        amount: Long
    ): SettlementResponse {
        return api.updateMyExpense(
            promiseId = promiseId,
            request = UpdateMyExpenseRequest(amount = amount)
        )
    }

    suspend fun settleExpenses(promiseId: Long): SettlementResponse {
        return api.settleExpenses(promiseId)
    }
}