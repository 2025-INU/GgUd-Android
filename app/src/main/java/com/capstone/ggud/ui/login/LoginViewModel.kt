package com.capstone.ggud.ui.login

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.capstone.ggud.data.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.user.UserApiClient

data class LoginUiState(
    val loading: Boolean = false,
    val errorMessage: String? = null,
    val success: Boolean = false
)

class LoginViewModel(
    private val repo: AuthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState

    fun loginWithKakao(context: Context) {
        if (_uiState.value.loading) return

        _uiState.value = LoginUiState(loading = true)

        val callback: (OAuthToken?, Throwable?) -> Unit = callback@{ token, error ->
            if (error != null) {
                _uiState.value = LoginUiState(
                    loading = false,
                    errorMessage = "카카오 로그인 실패: ${error.message}"
                )
                return@callback
            }
            if (token == null) {
                _uiState.value = LoginUiState(
                    loading = false,
                    errorMessage = "카카오 토큰이 비어있습니다."
                )
                return@callback
            }

            viewModelScope.launch {
                runCatching {
                    repo.loginWithKakaoSdk(token.accessToken)
                }.onSuccess {
                    _uiState.value = LoginUiState(loading = false, success = true)
                }.onFailure { t ->
                    _uiState.value = LoginUiState(
                        loading = false,
                        errorMessage = "서버 로그인 실패: ${t.message}"
                    )
                }
            }
        }

        if (UserApiClient.instance.isKakaoTalkLoginAvailable(context)) {
            UserApiClient.instance.loginWithKakaoTalk(context, callback = callback)
        } else {
            UserApiClient.instance.loginWithKakaoAccount(context, callback = callback)
        }
    }

    fun consumeError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}

class LoginViewModelFactory(
    private val repo: AuthRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return LoginViewModel(repo) as T
    }
}