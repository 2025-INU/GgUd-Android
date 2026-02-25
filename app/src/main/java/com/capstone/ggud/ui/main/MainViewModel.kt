package com.capstone.ggud.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.capstone.ggud.data.PromiseRepository
import com.capstone.ggud.network.dto.PromiseResponse
import com.capstone.ggud.network.dto.PromiseStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

data class MainUiState(
    val loading: Boolean = false,
    val inProgress: List<PromiseResponse> = emptyList(),
    val upcoming: List<PromiseResponse> = emptyList(),
    val error: String? = null
)

class MainViewModel(
    private val repo: PromiseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState(loading = true))
    val uiState: StateFlow<MainUiState> = _uiState

    init {
        load()
    }

    fun load() {
        _uiState.value = _uiState.value.copy(loading = true, error = null)

        viewModelScope.launch {
            val inProgressResult = runCatching {
                repo.getMyPromises(
                    status = PromiseStatus.IN_PROGRESS,
                    page = 0,
                    size = 50,
                    sort = listOf("promiseDateTime,asc")
                )
            }

            val upcomingResult = runCatching {
                repo.getMyPromises(
                    status = PromiseStatus.CONFIRMED,
                    page = 0,
                    size = 50,
                    sort = listOf("promiseDateTime,asc")
                )
            }

            val inProgress = inProgressResult.getOrNull()?.content ?: emptyList()
            val upcoming = upcomingResult.getOrNull()?.content ?: emptyList()

            val err = inProgressResult.exceptionOrNull()?.message
                ?: upcomingResult.exceptionOrNull()?.message

            _uiState.value = _uiState.value.copy(
                loading = false,
                inProgress = inProgress,
                upcoming = upcoming,
                error = err
            )
        }
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

class MainViewModelFactory(
    private val repo: PromiseRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}