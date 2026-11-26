package com.leaf.qrcodegenerator.viewmodel

import com.leaf.qrcodegenerator.R
import com.leaf.qrcodegenerator.data.QrCodeImageStore
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class QrCodeViewModelTest {
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
    fun `save success emits success message and resets saving state`() = runTest {
        val gate = CompletableDeferred<Boolean>()
        var saveCalls = 0
        val store = object : QrCodeImageStore {
            override suspend fun save(content: String): Boolean {
                saveCalls++
                return gate.await()
            }
        }
        val viewModel = QrCodeViewModel(store)

        viewModel.save("https://example.com")
        testScheduler.runCurrent()
        assertTrue(viewModel.uiState.value.isSaving)

        // 保存进行中忽略重复触发
        viewModel.save("https://example.com")
        testScheduler.runCurrent()
        assertEquals(1, saveCalls)

        gate.complete(true)
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isSaving)
        assertEquals(R.string.qr_code_save_success, viewModel.uiState.value.messageRes)

        viewModel.consumeMessage()
        assertNull(viewModel.uiState.value.messageRes)
    }

    @Test
    fun `save failure emits failed message`() = runTest {
        val store = object : QrCodeImageStore {
            override suspend fun save(content: String): Boolean = false
        }
        val viewModel = QrCodeViewModel(store)

        viewModel.save("https://example.com")
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isSaving)
        assertEquals(R.string.qr_code_save_failed, viewModel.uiState.value.messageRes)
    }

    @Test
    fun `missing storage permission emits consumable message`() = runTest {
        val store = object : QrCodeImageStore {
            override suspend fun save(content: String): Boolean = true
        }
        val viewModel = QrCodeViewModel(store)

        viewModel.showStoragePermissionMissing()
        assertEquals(R.string.qr_code_storage_permission_missing, viewModel.uiState.value.messageRes)

        viewModel.consumeMessage()
        assertNull(viewModel.uiState.value.messageRes)
    }
}
