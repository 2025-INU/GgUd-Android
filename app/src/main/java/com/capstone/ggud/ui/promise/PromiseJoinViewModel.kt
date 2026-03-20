package com.capstone.ggud.ui.promise

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.capstone.ggud.data.PromiseRepository
import com.capstone.ggud.network.dto.PromiseSummaryResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PromiseJoinUiState(
    val isLoading: Boolean = false,
    val summary: PromiseSummaryResponse? = null,
    val error: String? = null,
    val isSubmitting: Boolean = false,
    val submitSuccess: Boolean = false,
    val submitError: String? = null
)

class PromiseJoinViewModel(
    private val repository: PromiseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PromiseJoinUiState())
    val uiState: StateFlow<PromiseJoinUiState> = _uiState.asStateFlow()

    fun loadPromiseSummary(promiseId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            runCatching {
                repository.getPromiseSummary(promiseId)
            }.onSuccess { summary ->
                _uiState.value = PromiseJoinUiState(
                    isLoading = false,
                    summary = summary,
                    error = null
                )
            }.onFailure { e ->
                _uiState.value = PromiseJoinUiState(
                    isLoading = false,
                    summary = null,
                    error = e.message ?: "약속 요약 조회 실패"
                )
            }
        }
    }

    fun updateDeparture(
        promiseId: Long,
        latitude: Double,
        longitude: Double,
        address: String?
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isSubmitting = true,
                submitSuccess = false,
                submitError = null
            )

            runCatching {
                repository.updateDeparture(
                    promiseId = promiseId,
                    latitude = latitude,
                    longitude = longitude,
                    address = address
                )
            }.onSuccess {
                _uiState.value = _uiState.value.copy(
                    isSubmitting = false,
                    submitSuccess = true,
                    submitError = null
                )
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    isSubmitting = false,
                    submitSuccess = false,
                    submitError = e.message ?: "출발지 저장 실패"
                )
            }
        }
    }
}

class PromiseJoinViewModelFactory(
    private val repository: PromiseRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PromiseJoinViewModel::class.java)) {
            return PromiseJoinViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}