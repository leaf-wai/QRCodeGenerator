package com.leaf.qrcodegenerator.viewmodel

import com.leaf.qrcodegenerator.data.HistoryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `history operations update the exposed state`() = runTest {
        val repository = FakeHistoryRepository(listOf("first"))
        val viewModel = MainViewModel(repository)
        advanceUntilIdle()
        assertEquals(listOf("first"), viewModel.uiState.value.history)

        viewModel.saveHistory("second")
        advanceUntilIdle()
        assertEquals(listOf("second", "first"), viewModel.uiState.value.history)

        viewModel.deleteHistory(setOf("first"))
        advanceUntilIdle()
        assertEquals(listOf("second"), viewModel.uiState.value.history)
    }

    @Test
    fun `clipboard and permission dialog state are normalized`() = runTest {
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
    private val _history = MutableStateFlow(initialHistory)
    override val history: Flow<List<String>> = _history.asStateFlow()

    override suspend fun save(content: String) {
        if (content.isBlank()) return
        val updated = _history.value.toMutableList()
        updated.remove(content)
        updated.add(0, content)
        _history.value = updated
    }

    override suspend fun delete(contents: Set<String>) {
        if (contents.isEmpty()) return
        _history.value = _history.value.filterNot(contents::contains)
    }
}
