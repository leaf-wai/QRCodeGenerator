package com.leaf.qrcodegenerator.utils

import com.leaf.qrcodegenerator.ApplicationHolder

/**
 * 根据手机的分辨率从 dip(像素) 的单位 转成为 px
 */
fun Float.dip2px(): Int {
    val scale = ApplicationHolder.app.resources.displayMetrics.density
    return (this * scale + 0.5f).toInt()
}