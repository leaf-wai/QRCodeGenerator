package com.leaf.qrcodegenerator.viewmodel

import com.leaf.qrcodegenerator.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ScanViewModelTest {
    @Test
    fun `scan result remains available until dismissed`() {
        val viewModel = ScanViewModel()

        viewModel.showResult("https://example.com")

        assertEquals("https://example.com", viewModel.uiState.value.result)
        assertFalse(viewModel.uiState.value.isPickingImage)

        viewModel.dismissResult()
        assertNull(viewModel.uiState.value.result)
    }

    @Test
    fun `gallery failure stops loading and emits a consumable message`() {
        val viewModel = ScanViewModel()

        viewModel.startPickingImage()
        assertTrue(viewModel.uiState.value.isPickingImage)

        viewModel.showImageRecognitionFailed()
        assertFalse(viewModel.uiState.value.isPickingImage)
        assertEquals(
            R.string.scan_image_recognition_failed,
            viewModel.uiState.value.messageRes,
        )

        viewModel.consumeMessage()
        assertNull(viewModel.uiState.value.messageRes)
    }
}
