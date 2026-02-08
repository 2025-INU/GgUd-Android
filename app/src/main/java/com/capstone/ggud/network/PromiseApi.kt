package com.capstone.ggud.network

import com.capstone.ggud.network.dto.CreatePromiseRequest
import com.capstone.ggud.network.dto.PromiseResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface PromiseApi {
    @POST("/api/v1/promises")
    suspend fun createPromise(@Body body: CreatePromiseRequest): PromiseResponse
}