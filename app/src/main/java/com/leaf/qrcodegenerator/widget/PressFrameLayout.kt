package com.leaf.qrcodegenerator.widget

import android.view.animation.DecelerateInterpolator
import android.view.MotionEvent
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.annotation.Keep
import com.leaf.qrcodegenerator.utils.dip2px

/**
 * 作者： 哒啦啦
 * 创建时间： 2021/1/20
 * 描述： 简单模仿MIUI12控件按压效果
 * 内圈按压：控件整体下沉并伴随阴影变化
 * 外圈按压：控件向按压位置倾斜
 */
class PressFrameLayout : FrameLayout {
    private var parentWidth = 0 //父布局宽度
    private var parentHeight = 0 //父布局高度
    private var padding = 0
    private var cornerRadius = 0F
    private var shadeOffset = 0F
    private var paintBg = Paint(Paint.ANTI_ALIAS_FLAG)
    private var camera = Camera()
    private var cameraX = 0F //触摸点x轴方向偏移比例
    private var cameraY = 0F //触摸点y轴方向偏移比例
    private var colorBg = 0
    private val shadeAlpha = -0x56000000 //背景阴影透明度
    private var touchProgress = 1f //按压缩放动画控制
    private var cameraProgress = 0f //相机旋转（按压偏移）动画控制
    private var pressArea = TouchArea(0F, 0F, 0F, 0F) //按压效果区域
    private var isInPressArea = true //按压位置是在内圈还是外圈
    private val maxAngle = 5 //倾斜时的相机最大倾斜角度，deg
    private val scale = 0.98f //整体按压时的形变控制
    private var pressTime: Long = 0 //计算按压时间，小于500毫秒响应onClick()
    private var bitmap: Bitmap? = null
    private var srcRectF = Rect()
    private var dstRectF = RectF()

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (!isInPressArea) {
            camera.save()
            //相机在控件中心上方，在x，y轴方向旋转，形成控件倾斜效果
            camera.rotateX(maxAngle * cameraX * cameraProgress)
            camera.rotateY(maxAngle * cameraY * cameraProgress)
            canvas.translate(parentWidth / 2f, parentHeight / 2f)
            camera.applyToCanvas(canvas)
            //还原canvas坐标系
            canvas.translate(-parentWidth / 2f, -parentHeight / 2f)
            camera.restore()
        }
        //绘制阴影和背景
        paintBg.setShadowLayer(
            shadeOffset * touchProgress,
            0F,
            0F,
            colorBg and 0x00FFFFFF or shadeAlpha
        )
        if (bitmap != null) {
            canvas.drawBitmap(bitmap!!, srcRectF, dstRectF, paintBg)
        } else {
            canvas.drawRoundRect(
                dstRectF, cornerRadius, cornerRadius, paintBg
            )
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        parentHeight = getDefaultSize(suggestedMinimumHeight, heightMeasureSpec)
        parentWidth = getDefaultSize(suggestedMinimumWidth, widthMeasureSpec)
        dstRectF[padding.toFloat(), padding.toFloat(), (parentWidth - padding).toFloat()] =
            (parentHeight - padding).toFloat()
        //计算输入按压的内部范围,布局中心部分为内圈，其他为外圈
        pressArea.set(
            (parentWidth - 2 * padding) / 4f + padding,
            (parentHeight - 2 * padding) / 4f + padding,
            parentWidth - (parentWidth - 2 * padding) / 4f - padding,
            parentHeight - (parentWidth - 2 * padding) / 4f - padding
        )
    }

    /**
     * 判断是按压内圈还是外圈
     * @return true:按压内圈；false:按压外圈
     */
    private fun isInPressArea(x: Float, y: Float): Boolean {
        return x > pressArea.left && x < pressArea.right && y > pressArea.top && y < pressArea.bottom
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val animatorSet = AnimatorSet()
        val duration = 100 //按压动画时长
        var type = 0
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                pressTime = System.currentTimeMillis()
                type = 1
                isInPressArea = isInPressArea(event.x, event.y)
            }
            MotionEvent.ACTION_CANCEL -> type = 2
            MotionEvent.ACTION_UP -> {
                if (System.currentTimeMillis() - pressTime < 500) {
                    performClick()
                }
                type = 2
            }
        }
        if (isInPressArea) { //内圈按压效果
            if (type != 0) {
                val animX = ObjectAnimator.ofFloat(this, "scaleX", if (type == 1) 1F else scale, if (type == 1) scale else 1F)
                val animY = ObjectAnimator.ofFloat(this, "scaleY", if (type == 1) 1F else scale, if (type == 1) scale else 1F).setDuration(duration.toLong())
                val animZ = ObjectAnimator.ofFloat(
                    this,
                    "touchProgress",
                    if (type == 1) 1F else 0F,
                    if (type == 1) 0F else 1F
                ).setDuration(duration.toLong())
                animX.interpolator = DecelerateInterpolator()
                animY.interpolator = DecelerateInterpolator()
                animZ.interpolator = DecelerateInterpolator()
                animatorSet.playTogether(animX, animY, animZ)
                animatorSet.start()
            }
        } else { //外圈按压效果
            cameraX = (event.x - parentWidth / 2f) / ((parentWidth - 2 * padding) / 2f)
            if (cameraX > 1) cameraX = 1f
            if (cameraX < -1) cameraX = -1f
            cameraY = (event.y - parentHeight / 2f) / ((parentHeight - 2 * padding) / 2f)
            if (cameraY > 1) cameraY = 1f
            if (cameraY < -1) cameraY = -1f
            //坐标系调整
            val tmp = cameraX
            cameraX = -cameraY
            cameraY = tmp
            when (type) {
                1 -> ObjectAnimator.ofFloat(
                    this, "cameraProgress", 0f, 1f
                ).setDuration(duration.toLong()).start()
                2 -> ObjectAnimator.ofFloat(
                    this, "cameraProgress", 1f, 0f
                ).setDuration(duration.toLong()).start()
                else -> {
                }
            }
            invalidate()
        }
        return true
    }

    fun getTouchProgress(): Float {
        return touchProgress
    }

    @Keep
    fun setTouchProgress(touchProgress: Float) {
        this.touchProgress = touchProgress
        invalidate()
    }

    fun getCameraProgress(): Float {
        return cameraProgress
    }

    @Keep
    fun setCameraProgress(cameraProgress: Float) {
        this.cameraProgress = cameraProgress
        invalidate()
    }

    init {
        //取消硬件加速，否则低版本Android可能会绘制不了阴影
        setLayerType(LAYER_TYPE_SOFTWARE, null)
        //开启viewGroup的onDraw()
        setWillNotDraw(false)
        padding = 0F.dip2px()
        cornerRadius = 20F.dip2px().toFloat()
        shadeOffset = 0F.dip2px().toFloat()

        //View的background为颜色或者图片的两种情况

        if (background is ColorDrawable) {
            colorBg = (background as ColorDrawable).color
            paintBg.color = colorBg
        } else {
            bitmap = (background as BitmapDrawable).bitmap
            srcRectF = Rect(0, 0, bitmap!!.width, bitmap!!.height)
        }
        background = null
    }
}
