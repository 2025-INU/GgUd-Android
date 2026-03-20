package com.capstone.ggud.ui.promise

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.capstone.ggud.data.PromiseRepository
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class PromiseUiState(
    val title: String = "",
    val selectedDateMillis: Long? = null,
    val selectedHour: Int? = null,
    val selecteMinute: Int? = null,

    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val createdPromiseId: Long? = null
) {
    val isFormValid: Boolean =
        title.trim().isNotEmpty() &&
                selectedDateMillis != null &&
                selectedHour != null &&
                selecteMinute != null
}

class PromiseViewModel(
    private val repo: PromiseRepository
) : ViewModel() {

    var uiState by mutableStateOf(PromiseUiState())
        private set

    fun onTitleChange(newTitle: String) {
        uiState = uiState.copy(
            title = newTitle.take(50),
            errorMessage = null
        )
    }

    fun onDateSelected(millis: Long?) {
        uiState = uiState.copy(
            selectedDateMillis = millis,
            errorMessage = null
        )
    }

    fun onTimeSelected(hour: Int, minute: Int) {
        uiState = uiState.copy(
            selectedHour = hour,
            selecteMinute = minute,
            errorMessage = null
        )
    }

    fun consumeCreated() {
        uiState = uiState.copy(createdPromiseId = null)
    }

    fun createPromise() {
        if (!uiState.isFormValid || uiState.isLoading) return

        val title = uiState.title.trim()
        val dateMillis = uiState.selectedDateMillis ?: return
        val hour = uiState.selectedHour ?: return
        val minute = uiState.selecteMinute ?: return

        val iso = buildLocalDateTimeString(dateMillis, hour, minute)

        viewModelScope.launch {
            runCatching {
                repo.createPromise(title = title, promiseDateTimeIso = iso)
            }.onSuccess { res ->
                uiState = uiState.copy(
                    isLoading = false,
                    createdPromiseId = res.id
                )
            }.onFailure { e ->
                uiState = uiState.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "약속 생성에 실패했습니다."
                )
            }
        }
    }

    private fun buildLocalDateTimeString(dateMillis: Long, hour: Int, minute: Int): String {
        val zone = ZoneId.systemDefault()
        val localDate = Instant.ofEpochMilli(dateMillis).atZone(zone).toLocalDate()

        val ldt = LocalDateTime.of(
            localDate,
            LocalTime.of(hour, minute)
        )

        return ldt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"))
    }
}

class PromiseViewModelFactory(
    private val repo: PromiseRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PromiseViewModel::class.java)) {
            return PromiseViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}