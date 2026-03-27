package com.capstone.ggud.ui.promise

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.capstone.ggud.data.PromiseRepository
import com.capstone.ggud.network.ApiClient
import com.capstone.ggud.network.dto.ParticipantResponse
import com.capstone.ggud.network.dto.PromiseSummaryResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class WaitingRoomUiState(
    val loading: Boolean = false,
    val inviteLink: String? = null,

    val summaryLoading: Boolean = false,
    val summary: PromiseSummaryResponse? = null,

    val participantsLoading: Boolean = false,
    val participants: List<ParticipantResponse> = emptyList(),

    val midpointStarted: Boolean = false,

    val error: String? = null
) {
    val participantCount: Int get() = participants.size
    val allLocationsSubmitted: Boolean
        get() = participants.isNotEmpty() && participants.all { it.locationSubmitted }
}

class WaitingRoomViewModel(application: Application)
    : AndroidViewModel(application) {

    private val repo: PromiseRepository by lazy {
        val api = ApiClient.getPromiseApi(getApplication())
        PromiseRepository(api)
    }

    private val _uiState = MutableStateFlow(WaitingRoomUiState())
    val uiState: StateFlow<WaitingRoomUiState> = _uiState

    //약속 요약
    fun fetchSummary(promiseId: Long) {
        _uiState.value = _uiState.value.copy(summaryLoading = true, error = null)

        viewModelScope.launch {
            runCatching {
                repo.getPromiseSummary(promiseId)
            }.onSuccess { summary ->
                _uiState.value = _uiState.value.copy(
                    summaryLoading = false,
                    summary = summary,
                    error = null
                )
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    summaryLoading = false,
                    summary = null,
                    error = e.message ?: "약속 요약 조회 실패"
                )
            }
        }
    }

    //초대 링크
    fun fetchInviteLink(promiseId: Long) {
        _uiState.value = _uiState.value.copy(loading = true, error = null)

        viewModelScope.launch {
            runCatching {
                repo.getInviteLink(promiseId)
            }.onSuccess { link ->
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    inviteLink = link,
                    error = null
                )
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    inviteLink = null,
                    error = e.message ?: "초대 링크 조회 실패"
                )
            }
        }
    }

    fun clearInviteLink() {
        _uiState.value = _uiState.value.copy(inviteLink = null)
    }

    //참여자 목록
    fun fetchParticipants(promiseId: Long) {
        _uiState.value = _uiState.value.copy(participantsLoading = true, error = null)

        viewModelScope.launch {
            runCatching {
                repo.getPromiseParticipants(promiseId)
            }.onSuccess { list ->
                _uiState.value = _uiState.value.copy(
                    participantsLoading = false,
                    participants = list,
                    error = null
                )
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    participantsLoading = false,
                    participants = emptyList(),
                    error = e.message ?: "참여자 목록 조회 실패"
                )
            }
        }
    }

    //중간지점 페이지 이동
    fun startMidpointSelection(promiseId: Long) {
        _uiState.value = _uiState.value.copy(
            midpointStarted = false,
            error = null
        )

        viewModelScope.launch {
            runCatching {
                repo.startMidpointSelection(promiseId)
            }.onSuccess {
                _uiState.value = _uiState.value.copy(
                    midpointStarted = true,
                    error = null
                )
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    midpointStarted = false,
                    error = e.message ?: "중간지점 이동 실패"
                )
            }
        }
    }

    fun clearMidpointStarted() {
        _uiState.value = _uiState.value.copy(midpointStarted = false)
    }
}