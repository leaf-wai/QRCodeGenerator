package com.leaf.qrcodegenerator.viewmodel

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import com.leaf.qrcodegenerator.data.HistoryRepository
import com.leaf.qrcodegenerator.data.MmkvHistoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@Immutable
data class MainUiState(
    val history: List<String> = emptyList(),
    val clipboardText: String = "",
    val showCameraPermissionDialog: Boolean = false,
)

class MainViewModel(
    private val historyRepository: HistoryRepository = MmkvHistoryRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        MainUiState(history = historyRepository.getHistory()),
    )
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    fun refreshHistory() {
        _uiState.update { it.copy(history = historyRepository.getHistory()) }
    }

    fun updateClipboard(content: String) {
        _uiState.update { it.copy(clipboardText = content.trim()) }
    }

    fun saveHistory(content: String) {
        historyRepository.save(content)
        refreshHistory()
    }

    fun deleteHistory(contents: Set<String>) {
        historyRepository.delete(contents)
        refreshHistory()
    }

    fun showCameraPermissionDialog() {
        _uiState.update { it.copy(showCameraPermissionDialog = true) }
    }

    fun dismissCameraPermissionDialog() {
        _uiState.update { it.copy(showCameraPermissionDialog = false) }
    }
}
