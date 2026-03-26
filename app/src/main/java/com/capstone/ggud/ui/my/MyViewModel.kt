package com.capstone.ggud.ui.my

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.capstone.ggud.data.UserRepository
import com.capstone.ggud.network.dto.UserResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class UserUiState(
    val isLoading: Boolean = false,
    val user: UserResponse? = null,
    val nicknameInput: String = "",
    val profileImageUrlInput: String = "",
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val error: String? = null
)

class MyViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserUiState())
    val uiState: StateFlow<UserUiState> = _uiState.asStateFlow()

    init { getMyPage() }

    fun getMyPage() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null,
                saveSuccess = false
            )

            runCatching {
                userRepository.getMyPage()
            }.onSuccess { user ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    user = user,
                    nicknameInput = user.nickname,
                    profileImageUrlInput = user.profileImageUrl.orEmpty(),
                    error = null
                )
            }.onFailure { throwable ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = throwable.message ?: "사용자 정보를 불러오지 못함"
                )
            }
        }
    }

    fun onNicknameChanged(nickname: String) {
        _uiState.value = _uiState.value.copy(
            nicknameInput = nickname
        )
    }

    fun onProfileImageUrlChanged(profileImageUrl: String) {
        _uiState.value = _uiState.value.copy(
            profileImageUrlInput = profileImageUrl
        )
    }

    fun updateMyProfile() {
        val currentState = _uiState.value
        val nickname = currentState.nicknameInput.trim()
        val profileImageUrl = currentState.profileImageUrlInput.trim()

        if (nickname.isBlank()) {
            _uiState.value = currentState.copy(
                error = "닉네임을 입력해주세요"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = currentState.copy(
                isSaving = true,
                error = null,
                saveSuccess = false
            )

            runCatching {
                userRepository.updateMy(
                    nickname = nickname,
                    profileImageUrl = profileImageUrl
                )
            }.onSuccess { updatedUser ->
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    user = updatedUser,
                    nicknameInput = updatedUser.nickname,
                    profileImageUrlInput = updatedUser.profileImageUrl.orEmpty(),
                    saveSuccess = true,
                    error = null
                )
            }.onFailure { throwable ->
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    saveSuccess = false,
                    error = throwable.message ?: "프로필 수정 실패"
                )
            }
        }
    }

    fun clearSaveSuccess() {
        _uiState.value = _uiState.value.copy(
            saveSuccess = false
        )
    }

    fun clearErrorMessage() {
        _uiState.value = _uiState.value.copy(
            error = null
        )
    }
}

class MyViewModelFactory(
    private val userRepository: UserRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MyViewModel::class.java)) {
            return MyViewModel(userRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}