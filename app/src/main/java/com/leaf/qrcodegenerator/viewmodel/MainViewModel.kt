package com.leaf.qrcodegenerator.viewmodel

import android.content.Context
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.leaf.qrcodegenerator.data.DataStoreHistoryRepository
import com.leaf.qrcodegenerator.data.HistoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Immutable
data class MainUiState(
    val history: List<String> = emptyList(),
    val clipboardText: String = "",
    val showCameraPermissionDialog: Boolean = false,
)

class MainViewModel(
    private val historyRepository: HistoryRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        historyRepository.history
            .onEach { history ->
                _uiState.update { it.copy(history = history) }
            }
            .launchIn(viewModelScope)
    }

    fun updateClipboard(content: String) {
        _uiState.update { it.copy(clipboardText = content.trim()) }
    }

    fun saveHistory(content: String) {
        viewModelScope.launch {
            historyRepository.save(content)
        }
    }

    fun deleteHistory(contents: Set<String>) {
        if (contents.isEmpty()) return
        viewModelScope.launch {
            historyRepository.delete(contents)
        }
    }

    fun showCameraPermissionDialog() {
        _uiState.update { it.copy(showCameraPermissionDialog = true) }
    }

    fun dismissCameraPermissionDialog() {
        _uiState.update { it.copy(showCameraPermissionDialog = false) }
    }

    companion object {
        fun factory(context: Context): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    require(modelClass.isAssignableFrom(MainViewModel::class.java))
                    return MainViewModel(DataStoreHistoryRepository(context)) as T
                }
            }
    }
}
