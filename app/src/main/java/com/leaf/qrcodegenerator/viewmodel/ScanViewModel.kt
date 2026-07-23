package com.leaf.qrcodegenerator.viewmodel

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import com.leaf.qrcodegenerator.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@Immutable
data class ScanUiState(
    val result: String? = null,
    val isPickingImage: Boolean = false,
    @param:StringRes val messageRes: Int? = null,
)

class ScanViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ScanUiState())
    val uiState: StateFlow<ScanUiState> = _uiState.asStateFlow()

    fun startPickingImage() {
        _uiState.update { it.copy(isPickingImage = true) }
    }

    fun finishPickingImage() {
        _uiState.update { it.copy(isPickingImage = false) }
    }

    fun showResult(content: String) {
        _uiState.update { it.copy(result = content, isPickingImage = false) }
    }

    fun dismissResult() {
        _uiState.update { it.copy(result = null) }
    }

    fun showCameraPermissionMissing() = showMessage(R.string.scan_camera_permission_missing)

    fun showCameraStartFailed() = showMessage(R.string.scan_camera_start_failed)

    fun showCameraConnectFailed() = showMessage(R.string.scan_camera_connect_failed)

    fun showImageReadFailed() = showMessage(R.string.scan_image_read_failed)

    fun showImageNotFound() = showMessage(R.string.scan_image_no_qr_code)

    fun showImageRecognitionFailed() = showMessage(R.string.scan_image_recognition_failed)

    fun showCopiedMessage() = showMessage(R.string.common_copied_to_clipboard)

    fun consumeMessage() {
        _uiState.update { it.copy(messageRes = null) }
    }

    private fun showMessage(@StringRes messageRes: Int) {
        _uiState.update { it.copy(messageRes = messageRes, isPickingImage = false) }
    }
}
