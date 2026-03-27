package com.capstone.ggud.ui.recommendation

import com.capstone.ggud.network.dto.StationRecommendation

fun StationRecommendation.toMiddlePointCardUi(): MiddlePointCardUi {
    return MiddlePointCardUi(
        title = stationName,
        address = lineName,
        avgMinutes = averageTravelTimeMinutes
    )
}