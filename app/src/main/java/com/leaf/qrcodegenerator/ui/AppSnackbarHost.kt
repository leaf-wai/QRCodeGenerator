package com.leaf.qrcodegenerator.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import top.yukonga.miuix.kmp.basic.Snackbar
import top.yukonga.miuix.kmp.basic.SnackbarDefaults
import top.yukonga.miuix.kmp.basic.SnackbarHost
import top.yukonga.miuix.kmp.basic.SnackbarHostState

private val DarkSnackbarContainer = Color(0xFF2C2C2E)
private val DarkSnackbarContent = Color.White

@Composable
fun AppSnackbarHost(
    state: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    SnackbarHost(
        state = state,
        modifier = modifier,
    ) { data ->
        Snackbar(
            data = data,
            colors = SnackbarDefaults.snackbarColors(
                containerColor = DarkSnackbarContainer,
                contentColor = DarkSnackbarContent,
                actionContentColor = DarkSnackbarContent,
                dismissActionContentColor = DarkSnackbarContent,
            ),
        )
    }
}
