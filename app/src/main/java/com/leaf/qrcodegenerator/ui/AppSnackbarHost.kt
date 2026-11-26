package com.leaf.qrcodegenerator.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import top.yukonga.miuix.kmp.basic.Snackbar
import top.yukonga.miuix.kmp.basic.SnackbarDefaults
import top.yukonga.miuix.kmp.basic.SnackbarHost
import top.yukonga.miuix.kmp.basic.SnackbarHostState
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun AppSnackbarHost(
    state: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    val containerColor = MiuixTheme.colorScheme.onSurface
    val contentColor = MiuixTheme.colorScheme.surface
    SnackbarHost(
        state = state,
        modifier = modifier,
    ) { data ->
        Snackbar(
            data = data,
            colors = SnackbarDefaults.snackbarColors(
                containerColor = containerColor,
                contentColor = contentColor,
                actionContentColor = contentColor,
                dismissActionContentColor = contentColor,
            ),
        )
    }
}
