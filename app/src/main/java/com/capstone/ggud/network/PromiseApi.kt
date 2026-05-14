package com.capstone.ggud.network

import com.capstone.ggud.network.dto.ConfirmMidpointRequest
import com.capstone.ggud.network.dto.CreatePromiseRequest
import com.capstone.ggud.network.dto.DirectionResponse
import com.capstone.ggud.network.dto.InviteCodeResponse
import com.capstone.ggud.network.dto.MapDataResponse
import com.capstone.ggud.network.dto.MidpointRecommendationResponse
import com.capstone.ggud.network.dto.PagePromiseResponse
import com.capstone.ggud.network.dto.ParticipantResponse
import com.capstone.ggud.network.dto.PlaceConfirmRequest
import com.capstone.ggud.network.dto.PlaceRecommendationRequest
import com.capstone.ggud.network.dto.PlaceRecommendationResponse
import com.capstone.ggud.network.dto.PromiseArrivalResponse
import com.capstone.ggud.network.dto.PromiseResponse
import com.capstone.ggud.network.dto.PromiseStatus
import com.capstone.ggud.network.dto.PromiseSummaryResponse
import com.capstone.ggud.network.dto.SettlementResponse
import com.capstone.ggud.network.dto.UpdateDepartureRequest
import com.capstone.ggud.network.dto.UpdateMyExpenseRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface PromiseApi {
    @POST("/api/v1/promises")
    suspend fun createPromise(@Body body: CreatePromiseRequest): PromiseResponse

    @GET("/api/v1/promises")
    suspend fun getMyPromises(
        @Query("status") status: PromiseStatus? = null,
        @Query("keyword") keyword: String? = null,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): PagePromiseResponse

    @GET("/api/v1/promises/{promiseId}/participants")
    suspend fun getPromiseParticipants(
        @Path("promiseId") promiseId: Long
    ): List<ParticipantResponse>

    @GET("/api/v1/promises/{promiseId}/summary")
    suspend fun getPromiseSummary(
        @Path("promiseId") promiseId: Long
    ): PromiseSummaryResponse

    @PUT("/api/v1/promises/{promiseId}/departure")
    suspend fun updateDeparture(
        @Path("promiseId") promiseId: Long,
        @Body body: UpdateDepartureRequest
    )

    @POST("/api/v1/promises/{promiseId}/start-midpoint-selection")
    suspend fun startMidpointSelection(
        @Path("promiseId") promiseId: Long
    )

    @GET("/api/v1/promises/{promiseId}/midpoint/recommendations")
    suspend fun getMidpointRecommendations(
        @Path("promiseId") promiseId: Long
    ): MidpointRecommendationResponse

    @GET("/api/v1/promises/{promiseId}/invite/code")
    suspend fun getInviteCode(
        @Path("promiseId") promiseId: Long
    ): InviteCodeResponse

    @GET("/api/v1/promises/invite/{inviteCode}")
    suspend fun getPromiseByInviteCode(
        @Path("inviteCode") inviteCode: String
    ): PromiseResponse

    @POST("/api/v1/promises/{promiseId}/place-recommendations")
    suspend fun getPlaceRecommendations(
        @Path("promiseId") promiseId: Long,
        @Body body: PlaceRecommendationRequest
    ): PlaceRecommendationResponse

    @POST("/api/v1/promises/{promiseId}/midpoint/confirm")
    suspend fun confirmMidpoint(
        @Path("promiseId") promiseId: Long,
        @Body request: ConfirmMidpointRequest
    ): Response<Unit>

    @POST("/api/v1/promises/{promiseId}/midpoint/place-confirm")
    suspend fun confirmPlace(
        @Path("promiseId") promiseId: Long,
        @Body body: PlaceConfirmRequest
    )

    @POST("/api/v1/promises/join/{inviteCode}")
    suspend fun joinPromiseByInviteCode(
        @Path("inviteCode") inviteCode: String
    ): PromiseResponse

    @POST("/api/v1/promises/{promiseId}/midpoint/reset")
    suspend fun resetMidpoint(
        @Path("promiseId") promiseId: Long
    )

    @GET("/api/v1/promises/{promiseId}/directions")
    suspend fun getDirections(
        @Path("promiseId") promiseId: Long,
        @Query("originLat") originLat: Double,
        @Query("originLon") originLon: Double,
        @Query("destLat") destLat: Double,
        @Query("destLon") destLon: Double
    ): DirectionResponse

    @GET("/api/v1/promises/{promiseId}/map-data")
    suspend fun getMapData(
        @Path("promiseId") promiseId: Long
    ): MapDataResponse

    @GET("/api/v1/promises/{promiseId}/arrivals")
    suspend fun getPromiseArrivals(
        @Path("promiseId") promiseId: Long
    ): PromiseArrivalResponse

    @PATCH("/api/v1/promises/{promiseId}/complete")
    suspend fun completePromise(
        @Path("promiseId") promiseId: Long
    ): Response<Unit>

    @GET("/api/v1/promises/{promiseId}/expenses")
    suspend fun getExpenses(
        @Path("promiseId") promiseId: Long
    ): SettlementResponse

    @PUT("/api/v1/promises/{promiseId}/expenses/my")
    suspend fun updateMyExpense(
        @Path("promiseId") promiseId: Long,
        @Body request: UpdateMyExpenseRequest
    ): SettlementResponse

    @POST("/api/v1/promises/{promiseId}/expenses/settle")
    suspend fun settleExpenses(
        @Path("promiseId") promiseId: Long
    ): SettlementResponse
}