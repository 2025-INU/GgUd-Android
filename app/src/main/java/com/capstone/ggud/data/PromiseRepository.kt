package com.capstone.ggud.data

import com.capstone.ggud.network.PromiseApi
import com.capstone.ggud.network.dto.CreatePromiseRequest
import com.capstone.ggud.network.dto.PagePromiseResponse
import com.capstone.ggud.network.dto.ParticipantResponse
import com.capstone.ggud.network.dto.PromiseResponse
import com.capstone.ggud.network.dto.PromiseStatus
import com.capstone.ggud.network.dto.PromiseSummaryResponse
import com.capstone.ggud.network.dto.UpdateDepartureRequest

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

    suspend fun getInviteLink(promiseId: Long): String {
        return api.getInviteLink(promiseId).inviteUrl
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
}