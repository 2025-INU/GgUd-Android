package com.capstone.ggud.ui.recommendation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.capstone.ggud.data.PromiseRepository
import com.capstone.ggud.network.dto.PlaceRecommendationItem
import com.capstone.ggud.network.dto.PlaceRecommendationTab
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RecommendPlaceUiState(
    val isLoading: Boolean = false,
    val isConfirming: Boolean = false,
    val errorMessage: String? = null,
    val selectedTab: PlaceRecommendationTab = PlaceRecommendationTab.ALL,
    val aiQuery: String = "",
    val places: List<PlaceRecommendationItem> = emptyList(),
    val selectedPlaceIds: Set<String> = emptySet(),
    val confirmSuccess: Boolean = false
)

class RecommendPlaceViewModel(
    private val promiseId: Long,
    private val repository: PromiseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecommendPlaceUiState())
    val uiState: StateFlow<RecommendPlaceUiState> = _uiState.asStateFlow()

    init {
        loadPlaceRecommendations()
    }

    fun selectTab(tab: PlaceRecommendationTab) {
        _uiState.update {
            it.copy(
                selectedTab = tab,
                selectedPlaceIds = emptySet()
            )
        }
        loadPlaceRecommendations()
    }

    fun requestAiRecommendation(query: String) {
        _uiState.update {
            it.copy(
                aiQuery = query.trim(),
                selectedPlaceIds = emptySet()
            )
        }
        loadPlaceRecommendations()
    }

    fun clearAiQuery() {
        _uiState.update {
            it.copy(
                aiQuery = "",
                selectedPlaceIds = emptySet()
            )
        }
        loadPlaceRecommendations()
    }

    fun togglePlace(placeId: String) {
        _uiState.update { state ->
            val newSelectedIds =
                if (placeId in state.selectedPlaceIds) {
                    state.selectedPlaceIds - placeId
                } else {
                    state.selectedPlaceIds + placeId
                }

            state.copy(selectedPlaceIds = newSelectedIds)
        }
    }

    fun confirmAnySelectedPlace() {
        val state = _uiState.value

        val selectedPlace = state.places.firstOrNull {
            it.placeId in state.selectedPlaceIds
        } ?: return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isConfirming = true,
                    errorMessage = null,
                    confirmSuccess = false
                )
            }

            runCatching {
                repository.confirmPlace(
                    promiseId = promiseId,
                    placeId = selectedPlace.placeId,
                    placeName = selectedPlace.placeName,
                    latitude = selectedPlace.latitude,
                    longitude = selectedPlace.longitude
                )
            }.onSuccess {
                _uiState.update {
                    it.copy(
                        isConfirming = false,
                        confirmSuccess = true
                    )
                }
            }.onFailure {
                _uiState.update {
                    it.copy(
                        isConfirming = false,
                        errorMessage = "장소 확정에 실패했습니다."
                    )
                }
            }
        }
    }

    fun clearConfirmSuccess() {
        _uiState.update {
            it.copy(confirmSuccess = false)
        }
    }

    private fun loadPlaceRecommendations() {
        viewModelScope.launch {
            val currentState = _uiState.value

            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null
                )
            }

            runCatching {
                repository.getPlaceRecommendations(
                    promiseId = promiseId,
                    query = currentState.aiQuery,
                    tab = currentState.selectedTab
                )
            }.onSuccess { response ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        places = response.recommendations
                    )
                }
            }.onFailure {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "장소추천 실패"
                    )
                }
            }
        }
    }

    class Factory(
        private val promiseId: Long,
        private val repository: PromiseRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return RecommendPlaceViewModel(
                promiseId = promiseId,
                repository = repository
            ) as T
        }
    }
}