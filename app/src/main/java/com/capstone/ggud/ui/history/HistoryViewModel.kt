package com.capstone.ggud.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.capstone.ggud.data.PromiseRepository
import com.capstone.ggud.network.dto.PromiseResponse
import com.capstone.ggud.network.dto.PromiseStatus
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

data class HistoryUiState(
    val loading: Boolean = false,
    val items: List<PromiseResponse> = emptyList(),
    val error: String? = null,
    val keyword: String = ""
)

class HistoryViewModel(
    private val repo: PromiseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState(loading = true))
    val uiState: StateFlow<HistoryUiState> = _uiState

    private var searchJob: Job? = null

    init {
        loadHistory(status = PromiseStatus.COMPLETED, keyword = null)
    }

    fun loadHistory(status: PromiseStatus?, keyword: String?) {
        _uiState.value = _uiState.value.copy(loading = true, error = null)

        viewModelScope.launch {
            runCatching {
                repo.getMyPromises(
                    status = PromiseStatus.COMPLETED,
                    page = 0,
                    size = 50,
                    sort = listOf("createdAt,desc")
                )
            }.onSuccess { page ->
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    items = page.content,
                    error = null
                )
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    error = e.message ?: "히스토리 불러오기 실패"
                )
            }
        }
    }

    //검색어 입력 시 호출
    fun onKeywordChanged(newKeyword: String) {
        _uiState.value = _uiState.value.copy(keyword = newKeyword)

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(400)

            val kw = _uiState.value.keyword.trim()
            if (kw.isBlank()) {
                loadHistory(status = PromiseStatus.COMPLETED, keyword = null)
            } else {
                loadHistory(status = PromiseStatus.COMPLETED, keyword = kw)
            }
        }
    }

    fun clearKeyword() {
        _uiState.value = _uiState.value.copy(keyword = "")
        loadHistory(status = PromiseStatus.COMPLETED, keyword = null)
    }

    companion object {
        private val dateFmt = DateTimeFormatter.ofPattern("yyyy-M-d")
        private val timeFmt = DateTimeFormatter.ofPattern("HH:mm")

        fun formatDate(iso: String?): String {
            if (iso.isNullOrBlank()) return "-"
            return runCatching { OffsetDateTime.parse(iso).format(dateFmt) }.getOrDefault("-")
        }
        fun formatTime(iso: String?): String {
            if (iso.isNullOrBlank()) return "-"
            return runCatching { OffsetDateTime.parse(iso).format(timeFmt) }.getOrDefault("-")
        }
    }
}

class HistoryViewModelFactory(
    private val repo: PromiseRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HistoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HistoryViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}