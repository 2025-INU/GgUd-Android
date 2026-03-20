package com.capstone.ggud.data

import android.content.Context
import com.capstone.ggud.network.kakao.KakaoApiClient
import com.capstone.ggud.network.kakao.KakaoPlaceDocument

class KakaoLocalRepository(
    private val context: Context
) {
    suspend fun searchPlaces(query: String): List<KakaoPlaceDocument> {
        if (query.isBlank()) return emptyList()

        return try {
            val keywordResult = KakaoApiClient.getApi(context).searchKeyword(query = query)

            if (keywordResult.documents.isNotEmpty()) {
                keywordResult.documents
            } else {
                val addressResult = KakaoApiClient.getApi(context).searchAddress(query = query)
                addressResult.documents
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}