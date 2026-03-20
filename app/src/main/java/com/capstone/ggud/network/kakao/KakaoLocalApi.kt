package com.capstone.ggud.network.kakao

import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface KakaoLocalApi {

    @GET("v2/local/search/keyword.json")
    suspend fun searchKeyword(
        @Query("query") query: String,
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 10
    ): KakaoPlaceSearchResponse

    @GET("v2/local/search/address.json")
    suspend fun searchAddress(
        @Query("query") query: String,
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 10
    ): KakaoPlaceSearchResponse
}