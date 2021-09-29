package com.leaf.qrcodegenerator.utils

import android.animation.ObjectAnimator
import android.view.View
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart

object AnimateUtils {

    fun getGradientAlphaAnimation(
        view: View,
        startAlpha: Float,
        endAlpha: Float,
        duration: Int
    ): ObjectAnimator = ObjectAnimator.ofFloat(view, "alpha", startAlpha, endAlpha).apply {
        this.duration = duration.toLong()
        doOnStart {
            view.visibility = View.VISIBLE
        }
        doOnEnd {
            if (endAlpha == 0F) {
                view.visibility = View.GONE
                view.alpha = 1F
            }
        }
    }
}