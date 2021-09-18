package com.leaf.qrcodegenerator.utils

import android.app.Activity
import android.graphics.Color
import android.os.Build
import android.view.View
import android.view.WindowInsetsController
import android.view.WindowManager

object StatusBarUtil {
    fun lightStatusBar(activity: Activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return
        val window = activity.window
        val decorView = window.decorView
        if (Build.VERSION.SDK_INT >= 30) {
            window.insetsController?.setSystemBarsAppearance(
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
            )
        } else {
            decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
        window.statusBarColor = Color.WHITE
    }

    fun getStatusBarHeight(activity: Activity): Int {
        if ((activity.resources.getIdentifier(
                "status_bar_height",
                "dimen", "android"
            )) > 0
        ) {
            return activity.resources.getDimensionPixelSize(
                activity.resources.getIdentifier(
                    "status_bar_height",
                    "dimen", "android"
                )
            )
        }
        return 0
    }
}