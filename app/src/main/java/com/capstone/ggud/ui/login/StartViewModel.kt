package com.capstone.ggud.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.capstone.ggud.data.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

//StartScreen에서 어디로 이동할지 결정하는 라우트타입
sealed class StartRoute {
    data object None : StartRoute()
    data object ToLogin : StartRoute()
    data object ToHome : StartRoute()
    data class Error(val message: String) : StartRoute()
}

//StartScreen UI 상태 (로딩여부+다음라우트)
data class StartUiState(
    val loading: Boolean = true,
    val next: StartRoute = StartRoute.None
)

class StartViewModel(
    private val authRepo: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StartUiState())
    val uiState: StateFlow<StartUiState> = _uiState

    //앱 시작 시 저장된 토큰이 유효한지 확인해서 다음 화면 결정
    fun decideNext() {
        if (!_uiState.value.loading) return //중복 실행 방지

        viewModelScope.launch {
            runCatching {
                //토큰 만료 임박이면 refresh, 아니면 기존 accessToken으로
                val access = authRepo.refreshIfNeedded()
                if (!access.isNullOrBlank()) { //accessToken 있으면 홈으로
                    _uiState.value = StartUiState(loading = false, next = StartRoute.ToHome)
                } else { //없으면 로그인으로
                    _uiState.value = StartUiState(loading = false, next = StartRoute.ToLogin)
                }
            }.onFailure { t ->
                _uiState.value = StartUiState(
                    loading = false,
                    next = StartRoute.Error(t.message ?: "시작 처리 중 오류 발생")
                )
            }
        }
    }
}

class StartViewModelFactory(
    private val authRepo: AuthRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return StartViewModel(authRepo) as T
    }
}