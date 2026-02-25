package com.capstone.ggud.network

import com.capstone.ggud.network.dto.CreatePromiseRequest
import com.capstone.ggud.network.dto.PagePromiseResponse
import com.capstone.ggud.network.dto.PromiseResponse
import com.capstone.ggud.network.dto.PromiseStatus
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface PromiseApi {
    @POST("/api/v1/promises")
    suspend fun createPromise(@Body body: CreatePromiseRequest): PromiseResponse

    @GET("/api/v1/promises")
    suspend fun getMyPromises(
        @Query("status") status: PromiseStatus? = null,
        @Query("keyword") keyword: String? = null,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
        @Query("sort") sort: List<String> = listOf("createdAt,desc")
    ): PagePromiseResponse
}