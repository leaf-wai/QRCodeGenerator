package com.leaf.qrcodegenerator.viewmodel

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.leaf.qrcodegenerator.R
import com.leaf.qrcodegenerator.data.MediaStoreQrCodeImageStore
import com.leaf.qrcodegenerator.data.QrCodeImageStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Immutable
data class QrCodeUiState(
    val isSaving: Boolean = false,
    @param:StringRes val messageRes: Int? = null,
)

class QrCodeViewModel(
    private val imageStore: QrCodeImageStore,
) : ViewModel() {
    private val _uiState = MutableStateFlow(QrCodeUiState())
    val uiState: StateFlow<QrCodeUiState> = _uiState.asStateFlow()

    fun save(content: String) {
        if (_uiState.value.isSaving) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val messageRes = if (imageStore.save(content)) {
                R.string.qr_code_save_success
            } else {
                R.string.qr_code_save_failed
            }
            _uiState.value = QrCodeUiState(messageRes = messageRes)
        }
    }

    fun showStoragePermissionMissing() {
        _uiState.update {
            it.copy(messageRes = R.string.qr_code_storage_permission_missing)
        }
    }

    fun consumeMessage() {
        _uiState.update { it.copy(messageRes = null) }
    }

    companion object {
        fun factory(context: Context): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    require(modelClass.isAssignableFrom(QrCodeViewModel::class.java))
                    return QrCodeViewModel(MediaStoreQrCodeImageStore(context.contentResolver)) as T
                }
            }
    }
}
