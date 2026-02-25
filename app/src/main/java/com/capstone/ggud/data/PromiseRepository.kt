package com.capstone.ggud.data

import com.capstone.ggud.network.PromiseApi
import com.capstone.ggud.network.dto.CreatePromiseRequest
import com.capstone.ggud.network.dto.PagePromiseResponse
import com.capstone.ggud.network.dto.PromiseResponse
import com.capstone.ggud.network.dto.PromiseStatus

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
        size: Int = 20,
        sort: List<String> = listOf("createdAt,desc")
    ): PagePromiseResponse {
        return api.getMyPromises(
            status = status,
            keyword = keyword,
            page = page,
            size = size,
            sort = sort
        )
    }
}