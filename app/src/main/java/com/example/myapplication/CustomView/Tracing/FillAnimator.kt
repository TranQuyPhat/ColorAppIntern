package com.example.myapplication.CustomView.Tracing

import android.animation.ValueAnimator
import android.graphics.Canvas
import android.graphics.Paint
import androidx.core.animation.addListener
import com.example.myapplication.model.TracingRegion

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.graphics.*
import android.view.animation.LinearInterpolator
import kotlin.math.max

class FillAnimator(
    private val region: TracingRegion,
    private val startX: Float,
    private val startY: Float,
    private val color: Int,
    private val onUpdate: () -> Unit,
    private val onEnd: () -> Unit
) {
    private var animator: ValueAnimator? = null
    private var currentRadius: Float = 0f
    private val maxRadius: Float
    private val gradientPaint = Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    init {
        // Tính toán bán kính tối đa cần thiết
        val bounds = region.bounds
        val dx = max(
            max(startX - bounds.left, bounds.right - startX),
            max(startY - bounds.top, bounds.bottom - startY)
        )
        maxRadius = dx * 1.5f // Thêm margin để đảm bảo phủ kín
    }

    fun start(duration: Long = 500L) {
        animator?.cancel()
        animator = ValueAnimator.ofFloat(0f, maxRadius).apply {
            this.duration = duration
            interpolator = LinearInterpolator()

            addUpdateListener { animation ->
                currentRadius = animation.animatedValue as Float
                updateGradient()
                onUpdate()
            }

            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    onEnd()
                }
            })
            start()
        }
    }

    private fun updateGradient() {
        gradientPaint.shader = RadialGradient(
            startX, startY, currentRadius,
            color, color, Shader.TileMode.CLAMP
        )
    }

    fun draw(canvas: Canvas) {
        canvas.save()
        canvas.clipPath(region.path)
        canvas.drawCircle(startX, startY, currentRadius, gradientPaint)
        canvas.restore()
    }

    fun cancel() {
        animator?.cancel()
    }

    fun isRunning(): Boolean {
        return animator?.isRunning ?: false
    }
}