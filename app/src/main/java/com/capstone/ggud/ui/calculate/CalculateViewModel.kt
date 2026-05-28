package com.capstone.ggud.ui.calculate

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.capstone.ggud.data.PromiseRepository
import com.capstone.ggud.network.ApiClient
import com.capstone.ggud.network.dto.SettlementResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class CalculateUiState(
    val loading: Boolean = false,
    val settlement: SettlementResponse? = null,
    val myAmountText: String = "",
    val myUserId: Long? = null,
    val userEditedAmount: Boolean = false,
    val error: String? = null
)

class CalculateViewModel(
    application: Application,
    private val repo: PromiseRepository
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(CalculateUiState())
    val uiState: StateFlow<CalculateUiState> = _uiState

    private val userApi by lazy {
        ApiClient.getUserApi(getApplication())
    }

    fun fetchMe() {
        viewModelScope.launch {
            runCatching {
                userApi.getMyPage()
            }.onSuccess { me ->
                _uiState.value = _uiState.value.copy(
                    myUserId = me.id,
                    error = null
                )
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    error = e.message
                )
            }
        }
    }

    fun loadInitial(promiseId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true, error = null)

            runCatching {
                val me = userApi.getMyPage()
                val response = repo.getExpenses(promiseId)

                me to response
            }.onSuccess { (me, response) ->
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    myUserId = me.id,
                    settlement = response,
                    myAmountText = response.expenses
                        .firstOrNull { it.userId == me.id }
                        ?.paidAmount
                        ?.takeIf { it > 0 }
                        ?.toString()
                        ?: "",
                    error = null
                )
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    error = e.message
                )
            }
        }
    }

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
                        .firstOrNull { it.userId == _uiState.value.myUserId }
                        ?.paidAmount
                        ?.takeIf { it > 0 }
                        ?.toString()
                        ?: "",
                    userEditedAmount = false
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
        _uiState.value = _uiState.value.copy(
            myAmountText = digitsOnly,
            userEditedAmount = true
        )
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

    override fun <T : ViewModel> create(
        modelClass: Class<T>,
        extras: androidx.lifecycle.viewmodel.CreationExtras
    ): T {

        if (modelClass.isAssignableFrom(CalculateViewModel::class.java)) {

            val application =
                extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                    ?: throw IllegalStateException("Application not found")

            @Suppress("UNCHECKED_CAST")
            return CalculateViewModel(application, repo) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}