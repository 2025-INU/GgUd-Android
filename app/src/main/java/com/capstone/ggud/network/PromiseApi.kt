package com.capstone.ggud.network

import com.capstone.ggud.network.dto.CreatePromiseRequest
import com.capstone.ggud.network.dto.InviteLinkResponse
import com.capstone.ggud.network.dto.PagePromiseResponse
import com.capstone.ggud.network.dto.ParticipantResponse
import com.capstone.ggud.network.dto.PromiseResponse
import com.capstone.ggud.network.dto.PromiseStatus
import com.capstone.ggud.network.dto.PromiseSummaryResponse
import com.capstone.ggud.network.dto.UpdateDepartureRequest
import retrofit2.http.Body
import retrofit2.http.GET
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

    @GET("/api/v1/promises/{promiseId}/invite/link")
    suspend fun getInviteLink(
        @Path("promiseId") promiseId: Long
    ): InviteLinkResponse

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
}