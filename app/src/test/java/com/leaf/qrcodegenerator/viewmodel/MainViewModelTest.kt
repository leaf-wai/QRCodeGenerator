package com.leaf.qrcodegenerator.viewmodel

import com.leaf.qrcodegenerator.data.HistoryRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MainViewModelTest {
    @Test
    fun `history operations update the exposed state`() {
        val repository = FakeHistoryRepository(listOf("first"))
        val viewModel = MainViewModel(repository)

        viewModel.saveHistory("second")
        assertEquals(listOf("second", "first"), viewModel.uiState.value.history)

        viewModel.deleteHistory(setOf("first"))
        assertEquals(listOf("second"), viewModel.uiState.value.history)
    }

    @Test
    fun `clipboard and permission dialog state are normalized`() {
        val viewModel = MainViewModel(FakeHistoryRepository())

        viewModel.updateClipboard("  copied value  ")
        viewModel.showCameraPermissionDialog()

        assertEquals("copied value", viewModel.uiState.value.clipboardText)
        assertTrue(viewModel.uiState.value.showCameraPermissionDialog)

        viewModel.dismissCameraPermissionDialog()
        assertFalse(viewModel.uiState.value.showCameraPermissionDialog)
    }
}

private class FakeHistoryRepository(
    initialHistory: List<String> = emptyList(),
) : HistoryRepository {
    private val history = initialHistory.toMutableList()

    override fun getHistory(): List<String> = history.toList()

    override fun save(content: String) {
        history.remove(content)
        history.add(0, content)
    }

    override fun delete(contents: Set<String>) {
        history.removeAll(contents)
    }
}
