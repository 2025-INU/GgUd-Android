package com.capstone.ggud.ui.recommendation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.capstone.ggud.data.PromiseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MiddlePointUiState(
    val isLoading: Boolean = false,
    val items: List<MiddlePointCardUi> = emptyList(),
    val error: String? = null
)

class MiddlePointViewModel(
    private val repo: PromiseRepository,
    private val promiseId: Long
) : ViewModel() {

    private val _uiState = MutableStateFlow(MiddlePointUiState(isLoading = true))
    val uiState: StateFlow<MiddlePointUiState> = _uiState.asStateFlow()

    init {
        loadMidpointRecommendations()
    }

    fun confirmMidpoint(
        stationId: Long,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            repo.confirmMidpoint(
                promiseId = promiseId,
                stationId = stationId
            ).onSuccess {
                onSuccess()
            }.onFailure {
                onError(it.message ?: "중간지점 확정에 실패했습니다.")
            }
        }
    }

    fun loadMidpointRecommendations() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null
            )

            runCatching {
                repo.getMidpointRecommendations(promiseId)
            }.onSuccess { response ->
                val mappedItems = response.recommendedStations.map { station ->
                    station.toMiddlePointCardUi()
                }

                _uiState.value = MiddlePointUiState(
                    isLoading = false,
                    items = mappedItems,
                    error = null
                )
            }.onFailure { throwable ->
                _uiState.value = MiddlePointUiState(
                    isLoading = false,
                    items = emptyList(),
                    error = throwable.message ?: "중간지점 불러오기 실패"
                )
            }
        }
    }
}

class MiddlePointViewModelFactory(
    private val repo: PromiseRepository,
    private val promiseId: Long
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MiddlePointViewModel::class.java)) {
            return MiddlePointViewModel(repo, promiseId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}