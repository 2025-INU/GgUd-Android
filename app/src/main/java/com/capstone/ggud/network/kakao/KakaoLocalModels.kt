package com.capstone.ggud.network.kakao

data class KakaoPlaceSearchResponse(
    val documents: List<KakaoPlaceDocument>,
    val meta: KakaoMeta
)

data class KakaoMeta(
    val is_end: Boolean,
    val pageable_count: Int,
    val total_count: Int
)

data class KakaoPlaceDocument(
    val id: String?,
    val place_name: String?,
    val address_name: String?,
    val road_address_name: String?,
    val x: String?,   // longitude
    val y: String?    // latitude
)