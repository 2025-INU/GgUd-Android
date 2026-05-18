package com.capstone.ggud.ui.my

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.capstone.ggud.data.AuthRepository
import com.capstone.ggud.data.UserRepository
import com.capstone.ggud.network.dto.UserResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

data class UserUiState(
    val isLoading: Boolean = false,
    val user: UserResponse? = null,
    val nicknameInput: String = "",
    val profileImageUrlInput: String = "",
    val selectedImageUri: Uri? = null,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val isLoggingOut: Boolean = false,
    val logoutSuccess: Boolean = false,
    val error: String? = null
)

class MyViewModel(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserUiState())
    val uiState: StateFlow<UserUiState> = _uiState.asStateFlow()

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

    fun onProfileImageSelected(uri: Uri) {
        _uiState.value = _uiState.value.copy(
            selectedImageUri = uri
        )
    }

    fun updateMyProfile(context: Context) {
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
                val updatedUser = userRepository.updateMy(
                    nickname = nickname,
                    profileImageUrl = profileImageUrl
                )

                Log.d("ProfileEdit", "selectedImageUri = ${currentState.selectedImageUri}")

                if (currentState.selectedImageUri != null) {
                    Log.d("ProfileEdit", "이미지 업로드 시작")
                    val imagePart = createImagePart(context, currentState.selectedImageUri)
                    userRepository.uploadProfileImage(imagePart)
                } else {
                    updatedUser
                }
            }.onSuccess { updatedUser ->
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    user = updatedUser,
                    nicknameInput = updatedUser.nickname,
                    profileImageUrlInput = updatedUser.profileImageUrl.orEmpty(),
                    selectedImageUri = null,
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

    private fun createImagePart(context: Context, uri: Uri): MultipartBody.Part {
        val contentResolver = context.contentResolver

        val mimeType = contentResolver.getType(uri) ?: "image/jpeg"
        val extension = when (mimeType) {
            "image/png" -> ".png"
            "image/gif" -> ".gif"
            "image/jpeg" -> ".jpg"
            else -> ".jpg"
        }

        val inputStream = contentResolver.openInputStream(uri)
            ?: throw IllegalArgumentException("이미지 파일을 열 수 없습니다")

        val file = File.createTempFile("profile_image", extension, context.cacheDir)
        inputStream.use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        val requestBody = file.asRequestBody("image/*".toMediaType())

        return MultipartBody.Part.createFormData(
            name = "image",
            filename = file.name,
            body = requestBody
        )
    }

    fun clearSaveSuccess() {
        _uiState.value = _uiState.value.copy(
            saveSuccess = false
        )
    }

    fun logout() {
        if (_uiState.value.isLoggingOut) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoggingOut = true,
                error = null
            )

            runCatching {
                authRepository.logout()
            }.onSuccess {
                _uiState.value = _uiState.value.copy(
                    isLoggingOut = false,
                    logoutSuccess = true
                )
            }.onFailure { throwable ->
                _uiState.value = _uiState.value.copy(
                    isLoggingOut = false,
                    error = throwable.message ?: "로그아웃 실패"
                )
            }
        }
    }

    fun clearErrorMessage() {
        _uiState.value = _uiState.value.copy(
            error = null
        )
    }
}

class MyViewModelFactory(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MyViewModel::class.java)) {
            return MyViewModel(userRepository, authRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}