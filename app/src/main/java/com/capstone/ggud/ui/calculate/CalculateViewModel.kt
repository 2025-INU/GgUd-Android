package com.capstone.ggud.ui.calculate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.capstone.ggud.data.PromiseRepository
import com.capstone.ggud.network.dto.SettlementResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class CalculateUiState(
    val loading: Boolean = false,
    val settlement: SettlementResponse? = null,
    val myAmountText: String = "",
    val error: String? = null
)

class CalculateViewModel(
    private val repo: PromiseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalculateUiState())
    val uiState: StateFlow<CalculateUiState> = _uiState

    fun loadExpenses(promiseId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true, error = null)

            runCatching {
                repo.getExpenses(promiseId)
            }.onSuccess { response ->
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    settlement = response,
                    myAmountText = response.expenses
                        .firstOrNull()
                        ?.paidAmount
                        ?.takeIf { it > 0 }
                        ?.toString()
                        ?: ""
                )
            }.onFailure {
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    error = it.message
                )
            }
        }
    }

    fun changeAmount(text: String) {
        val digitsOnly = text.filter { it.isDigit() }.take(12)
        _uiState.value = _uiState.value.copy(myAmountText = digitsOnly)
    }

    fun submitMyExpense(promiseId: Long) {
        val amount = _uiState.value.myAmountText.toLongOrNull() ?: 0L

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true, error = null)

            runCatching {
                repo.updateMyExpense(promiseId, amount)
            }.onSuccess { response ->
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    settlement = response
                )
            }.onFailure {
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    error = it.message
                )
            }
        }
    }

    fun settleExpenses(
        promiseId: Long,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                loading = true,
                error = null
            )

            runCatching {
                repo.settleExpenses(promiseId)
            }.onSuccess { response ->
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    settlement = response
                )
                onSuccess()
            }.onFailure {
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    error = it.message
                )
            }
        }
    }
}

class CalculateViewModelFactory(
    private val repo: PromiseRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CalculateViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CalculateViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}