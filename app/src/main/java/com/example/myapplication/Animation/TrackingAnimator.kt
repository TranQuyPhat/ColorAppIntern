package com.example.myapplication.Animation


import android.animation.Animator
import android.animation.ValueAnimator
import android.graphics.*
import android.view.View
import android.view.animation.LinearInterpolator
import com.example.myapplication.CustomView.Tracing.PenNib
import com.example.myapplication.model.TracingRegion
import kotlin.math.hypot

class TracingAnimator(
    private val view: View,
    private val regions: List<TracingRegion>,
    private val fillPaint: Paint,
    private val penNib: PenNib,
    private val onFillComplete: () -> Unit,
    private val onDrawingAnimationComplete: () -> Unit
) {
    private var totalLength = 0f
    private val pathMeasures = mutableListOf<PathMeasure>()
    private val animatedPath = Path()
    private var currentFraction = 0f
    private var animator: ValueAnimator? = null
    private var fillAnimator: ValueAnimator? = null

    var isAnimating = false
        private set
    var isDrawingAnimationComplete = false
        private set

    var fillCenter: PointF? = null
        private set
    var fillRadius: Float = 0f
        private set
    var animatingRegion: TracingRegion? = null
        private set

    fun prepareAnimation() {
        pathMeasures.clear()
        totalLength = 0f
        regions.forEach { region ->
            val pm = PathMeasure(region.path, false)
            pathMeasures.add(pm)
            totalLength += pm.length
        }
        currentFraction = 0f
        animatedPath.reset()
        isAnimating = false
        isDrawingAnimationComplete = false
    }

    fun startDrawingAnimation(fromFraction: Float = currentFraction) {
        if (isAnimating || totalLength <= 0) return

        animator?.cancel()
        animator = ValueAnimator.ofFloat(currentFraction, 1f).apply {
            duration = (10000 * (1 - currentFraction)).toLong()
            interpolator = LinearInterpolator()

            addUpdateListener {
                currentFraction = it.animatedValue as Float
                updateAnimatedPath(currentFraction)
                view.invalidate()
            }

            addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {
                    isAnimating = true
                }

                override fun onAnimationEnd(animation: Animator) {
                    isAnimating = false
                    if (currentFraction >= 1f) {
                        isDrawingAnimationComplete = true
                        onDrawingAnimationComplete()
                    }
                }

                override fun onAnimationCancel(animation: Animator) {
                    isAnimating = false
                }

                override fun onAnimationRepeat(animation: Animator) {}
            })

            start()
        }
    }

    fun stopDrawingAnimation() {
        animator?.cancel()
        isAnimating = false
    }

    fun getAnimatedPath(): Path = animatedPath

    private fun updateAnimatedPath(fraction: Float) {
        animatedPath.reset()
        var accumulated = 0f
        val target = fraction * totalLength
        val pos = FloatArray(2)
        val tan = FloatArray(2)

        for (pm in pathMeasures) {
            val segLen = pm.length
            val pathSegment = Path()
            if (accumulated + segLen < target) {
                pm.getSegment(0f, segLen, pathSegment, true)
                animatedPath.addPath(pathSegment)
                accumulated += segLen
            } else {
                val distanceInSegment = target - accumulated
                pm.getSegment(0f, distanceInSegment, pathSegment, true)
                animatedPath.addPath(pathSegment)

                pm.getPosTan(distanceInSegment, pos, tan)
                penNib.updatePosition(pos[0], pos[1], tan)
                break
            }
        }
    }

    fun startFillAnimation(x: Float, y: Float, region: TracingRegion) {
        fillCenter = PointF(x, y)
        animatingRegion = region

        val bounds = region.bounds
        val maxRadius = hypot(bounds.width(), bounds.height()) * 1.2f

        fillAnimator?.cancel()
        fillAnimator = ValueAnimator.ofFloat(0f, maxRadius).apply {
            duration = 500
            interpolator = LinearInterpolator()
            addUpdateListener {
                fillRadius = it.animatedValue as Float
                view.invalidate()
            }
            addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {}
                override fun onAnimationEnd(animation: Animator) {
                    region.isFilled = true
                    region.fillColor = fillPaint.color
                    animatingRegion = null
                    fillCenter = null
                    onFillComplete()
                    view.invalidate()
                }

                override fun onAnimationCancel(animation: Animator) {}
                override fun onAnimationRepeat(animation: Animator) {}
            })
                    start()
        }
    }
}