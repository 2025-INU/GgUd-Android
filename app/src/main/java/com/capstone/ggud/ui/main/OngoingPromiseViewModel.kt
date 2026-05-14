package com.capstone.ggud.ui.main

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.capstone.ggud.data.PromiseLocationSocket
import com.capstone.ggud.data.PromiseRepository
import com.capstone.ggud.network.ApiClient
import com.capstone.ggud.network.dto.RouteOption
import com.capstone.ggud.ui.map.OngoingParticipantLocation
import kotlinx.coroutines.launch

data class OngoingPromiseUiState(
    val isLoading: Boolean = false,
    val promiseTitle: String = "",
    val destinationLat: Double? = null,
    val destinationLon: Double? = null,
    val routeOptions: List<RouteOption> = emptyList(),
    val participantLocations: List<OngoingParticipantLocation> = emptyList(),
    val totalCount: Int = 0,
    val arrivedCount: Int = 0,
    val errorMessage: String? = null
)

class OngoingPromiseViewModel(
    private val promiseId: Long,
    private val repository: PromiseRepository
) : ViewModel() {

    var uiState by mutableStateOf(
        OngoingPromiseUiState()
    )
        private set

    var selectedRouteIndex by mutableIntStateOf(0)
        private set

    private var hasLoaded = false

    private var locationSocket: PromiseLocationSocket? = null

    init {
        loadArrivals()
    }

    fun selectRoute(index: Int) {
        selectedRouteIndex = index
    }

    private fun loadArrivals() {
        viewModelScope.launch {
            runCatching {
                repository.getPromiseArrivals(promiseId)
            }.onSuccess { response ->
                uiState = uiState.copy(
                    totalCount = response.totalCount,
                    arrivedCount = response.arrivedCount
                )
            }.onFailure {
                it.printStackTrace()
            }
        }
    }

    fun load(
        originLat: Double,
        originLon: Double
    ) {
        if (hasLoaded) return
        hasLoaded = true

        viewModelScope.launch {
            runCatching {

                uiState = uiState.copy(
                    isLoading = true,
                    errorMessage = null
                )

                val mapData = repository.getMapData(
                    promiseId = promiseId
                )

                val destination = mapData.destination

                val directions = if (destination != null) {
                    repository.getDirections(
                        promiseId = promiseId,
                        originLat = originLat,
                        originLon = originLon,
                        destLat = destination.latitude,
                        destLon = destination.longitude
                    )
                } else null

                uiState = uiState.copy(
                    isLoading = false,
                    destinationLat = destination?.latitude,
                    destinationLon = destination?.longitude,
                    routeOptions = directions?.routeOptions ?: emptyList(),
                    participantLocations =
                        mapData.currentLocations.map { participant ->
                            OngoingParticipantLocation(
                                userId = participant.userId,
                                nickname = participant.nickname,
                                latitude = participant.latitude,
                                longitude = participant.longitude,
                                isArrived = false
                            )
                        }
                )
            }.onFailure { throwable ->
                hasLoaded = false
                uiState = uiState.copy(
                    isLoading = false,
                    errorMessage = throwable.message
                )
            }
        }
    }

    fun connectLocationSocket(token: String) {
        if (locationSocket != null) return

        locationSocket = PromiseLocationSocket(
            token = token,
            promiseId = promiseId,
            onLocationReceived = { location ->
                val updatedLocations =
                    uiState.participantLocations
                        .filterNot { it.userId == location.userId } +
                            OngoingParticipantLocation(
                                userId = location.userId,
                                nickname = location.nickname,
                                latitude = location.latitude,
                                longitude = location.longitude,
                                isArrived = false
                            )

                uiState = uiState.copy(
                    participantLocations = updatedLocations
                )
            }
        )

        locationSocket?.connect()
    }

    fun sendMyLocation(
        latitude: Double,
        longitude: Double
    ) {
        locationSocket?.sendLocation(
            latitude = latitude,
            longitude = longitude
        )
    }

    override fun onCleared() {
        super.onCleared()
        locationSocket?.disconnect()
    }
}

class OngoingPromiseViewModelFactory(
    private val context: Context,
    private val promiseId: Long
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {

        val repository = PromiseRepository(
            ApiClient.getPromiseApi(context)
        )

        return OngoingPromiseViewModel(
            promiseId = promiseId,
            repository = repository
        ) as T
    }
}