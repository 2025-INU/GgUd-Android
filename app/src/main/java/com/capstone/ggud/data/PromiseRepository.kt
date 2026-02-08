package com.capstone.ggud.data

import com.capstone.ggud.network.PromiseApi
import com.capstone.ggud.network.dto.CreatePromiseRequest
import com.capstone.ggud.network.dto.PromiseResponse

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
}