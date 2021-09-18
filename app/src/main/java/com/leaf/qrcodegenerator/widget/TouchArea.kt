package com.leaf.qrcodegenerator.widget

data class TouchArea(
    var left: Float = 0F,
    var top: Float = 0F,
    var right: Float = 0F,
    var bottom: Float = 0F
) {
    fun set(left: Float, top: Float, right: Float, bottom: Float) {
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
    }
}

