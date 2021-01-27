package com.leaf.qrcodegenerator.utils

import android.app.Activity
import android.graphics.Color
import android.os.Build
import android.view.View
import android.view.WindowManager

object StatusBarUtil {
    fun fitSystemBar(activity: Activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return
        val window = activity.window
        val decorView = window.decorView
        decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
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