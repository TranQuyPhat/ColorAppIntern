package com.example.myapplication.CustomView.Tracing

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.Toast
import androidx.annotation.DrawableRes
import com.example.myapplication.R
import com.example.myapplication.model.TracingRegion
import java.io.InputStream
import kotlin.math.hypot


class TracingView2(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {
    private var drawingCompleteListener: OnDrawingCompleteListener? = null

    fun setOnDrawingCompleteListener(listener: OnDrawingCompleteListener) {
        drawingCompleteListener = listener
    }

    private val regions = mutableListOf<TracingRegion>()

    private val dashedPaint = Paint().apply {
        color = Color.BLACK
        strokeWidth = 2f
        style = Paint.Style.STROKE
        isAntiAlias = true
        pathEffect = DashPathEffect(floatArrayOf(20f, 20f), 0f)
    }

    private val fillPaint = Paint().apply {
        style = Paint.Style.FILL
        color = Color.TRANSPARENT
        isAntiAlias = true
    }

    private val animationPaint = Paint().apply {
        color = Color.RED
        strokeWidth = 2f
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    private val animatedPath = Path()
    private val pathMeasures = mutableListOf<PathMeasure>()
    private var totalLength = 0f
    private var currentFraction = 0f
    private var animator: ValueAnimator? = null
    private var isAnimating = false
    private var isDrawingAnimationComplete = false
    private lateinit var penNib: PenNib
    private val pathManager = PathManager()
    private var viewWidth = 0
    private var viewHeight = 0
    private var transformationMatrix = Matrix()
    private var fillCenter: PointF? = null
    private var fillRadius = 0f
    private var fillAnimator: ValueAnimator? = null
    private var animatingRegion: TracingRegion? = null
    fun setPenNibResource(@DrawableRes resId: Int) {
        penNib = PenNib(context, resId, shouldRotate = false)
        invalidate() // Vẽ lại view
    }
    init {
        setLayerType(LAYER_TYPE_HARDWARE, null)
        penNib = PenNib(context, R.drawable.pen1, shouldRotate = false)
    }

    fun loadSVGFromAsset(inputStream: InputStream) {
        pathManager.loadSVG(inputStream)
        originalPathsToRegions(pathManager.originalPaths)
        if (viewWidth > 0 && viewHeight > 0) {
            transformRegions()
            prepareAnimation()
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        viewWidth = w; viewHeight = h
        if (regions.isNotEmpty()) {
            transformRegions()
            prepareAnimation()
        }
    }

    private fun originalPathsToRegions(paths: List<Path>) {
        regions.clear()
        paths.forEachIndexed { index, path ->
            val boundsRect = RectF().apply { path.computeBounds(this, true) }
            regions.add(TracingRegion(index, Path(path), boundsRect))
        }
        regions.forEachIndexed { index, region ->
            Log.d("TracingView2", "Region $index: ID=${region.id}, Bounds=${region.bounds}, IsFilled=${region.isFilled}, FillColor=${String.format("#%08X", region.fillColor)}")
        }
    }

    private fun transformRegions() {
        val totalBounds = RectF()
        val tempRect = RectF()
        var first = true
        regions.forEach { region ->
            region.path.computeBounds(tempRect, true)
            if (first) {
                totalBounds.set(tempRect)
                first = false
            } else {
                totalBounds.union(tempRect)
            }
        }
        val scaleX = viewWidth / totalBounds.width()
        val scaleY = viewHeight / totalBounds.height()
        val scale = minOf(scaleX, scaleY) * 0.9f
        transformationMatrix.reset()
        transformationMatrix.postScale(scale, scale)
        val tx = (viewWidth - totalBounds.width() * scale) / 2 - totalBounds.left * scale
        val ty = (viewHeight - totalBounds.height() * scale) / 2 - totalBounds.top * scale
        transformationMatrix.postTranslate(tx, ty)

        regions.forEach { region ->
            val transformedPath = Path(region.path)
            transformedPath.transform(transformationMatrix)
            region.path.set(transformedPath)
            transformedPath.computeBounds(tempRect, true)
            region.bounds.set(tempRect)
        }
        invalidate()
    }

    private fun prepareAnimation() {
        pathMeasures.clear()
        totalLength = 0f
        regions.forEachIndexed { index, region ->
            val pm = PathMeasure(region.path, false)
            Log.d("TracingView2", "Region $index: Path Length=${pm.length}")
            pathMeasures.add(pm)
            totalLength += pm.length
        }
        Log.d("TracingView2", "Total Length of all paths: $totalLength")
        currentFraction = 0f
        animatedPath.reset()
        isAnimating = false
        isDrawingAnimationComplete = false
    }

    private fun startDrawingAnimation(fromFraction: Float = currentFraction) {
        if (isAnimating || totalLength <= 0) return

        animator?.cancel()
        animator = ValueAnimator.ofFloat(currentFraction, 1f).apply {
            duration = (10000 * (1 - currentFraction)).toLong()
            interpolator = LinearInterpolator()

            addUpdateListener { animation ->
                currentFraction = animation.animatedValue as Float
                updateAnimatedPath(currentFraction)
                invalidate()
            }

            addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {
                    isAnimating = true
                }

                override fun onAnimationEnd(animation: Animator) {
                    isAnimating = false
                    if (currentFraction >= 1f) {
                        isDrawingAnimationComplete = true
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


    private fun updateAnimatedPath(fraction: Float) {
        animatedPath.reset()
        var accumulated = 0f
        val target = fraction * totalLength
        val pos = FloatArray(2)
        val tan = FloatArray(2)

        for (pm in pathMeasures) {
            val segLen = pm.length
            val pathSegment = Path()
            if (accumulated + segLen < target) {                                //ve toan bo segment
                pm.getSegment(0f, segLen, pathSegment, true)
                animatedPath.addPath(pathSegment)
                accumulated += segLen
            } else {                                                             //ve 1 phan cua segment
                val distanceInSegment = target - accumulated
                pm.getSegment(0f, distanceInSegment, pathSegment, true)
                animatedPath.addPath(pathSegment)

                pm.getPosTan(distanceInSegment, pos, tan)
                penNib.updatePosition(pos[0], pos[1], tan)
                break
            }
        }
    }

    private val nextUnfilledRegion: TracingRegion?
        get() = regions.firstOrNull { !it.isFilled }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        regions.forEach { region ->
            if (region == nextUnfilledRegion && isDrawingAnimationComplete) {
                val highlightStroke = Paint(dashedPaint).apply {
                    color = Color.BLUE
                    strokeWidth = 5f
                    pathEffect = null
                }
                canvas.drawPath(region.path, highlightStroke)
            } else {
                canvas.drawPath(region.path, dashedPaint)
            }

            if (region.isFilled) {
                canvas.drawPath(region.path, Paint(fillPaint).apply { color = region.fillColor })
            } else if (region == animatingRegion && fillCenter != null) {
                val saveCount = canvas.save()
                val clipPath = Path().apply {
                    addCircle(fillCenter!!.x, fillCenter!!.y, fillRadius, Path.Direction.CW)
                }
                canvas.clipPath(clipPath)
                canvas.drawPath(region.path, Paint(fillPaint).apply { color = fillPaint.color })
                canvas.restoreToCount(saveCount)
            }
        }
        if (!isDrawingAnimationComplete) {
            canvas.drawPath(animatedPath, animationPaint)
            penNib.draw(canvas)
        } else {
            val stroke = Paint().apply {
                color = Color.BLACK
                strokeWidth = 2f
                style = Paint.Style.STROKE
                isAntiAlias = true
            }
            regions.forEach { region ->
                canvas.drawPath(region.path, stroke)
            }

        }
    }

    private fun stopDrawingAnimation() {
        animator?.cancel()
        isAnimating = false
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {

            MotionEvent.ACTION_DOWN -> {
                if (!isDrawingAnimationComplete) {
                    startDrawingAnimation(currentFraction)
                    return true
                }
                val x = event.x.toInt()
                val y = event.y.toInt()

                val sortedRegions = regions.sortedByDescending { it.id }

                for (region in sortedRegions) {
                    val targetRegion = nextUnfilledRegion
                    if (targetRegion != null) {
                        val hitRegion = Region().apply {
                            setPath(
                                targetRegion.path,
                                Region(0, 0, width, height)
                            )
                        }

                        if (hitRegion.contains(x, y)) {
                            startFillAnimation(x.toFloat(), y.toFloat(), targetRegion)
                            return true
                        }
                    }
                }
            }

            MotionEvent.ACTION_UP -> {
                if (isAnimating) {
                    stopDrawingAnimation()
                    return true
                }
            }
        }
        return super.onTouchEvent(event)
    }

    private fun startFillAnimation(x: Float, y: Float, region: TracingRegion) {
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
                invalidate()
            }
            addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {}
                override fun onAnimationEnd(animation: Animator) {
                    region.isFilled = true
                    region.fillColor = fillPaint.color
                    animatingRegion = null
                    fillCenter = null
                    checkDrawingComplete()
                    invalidate()
                }

                override fun onAnimationCancel(animation: Animator) {}
                override fun onAnimationRepeat(animation: Animator) {}
            })
            start()
        }
    }


    private fun checkDrawingComplete() {
        if (regions.all { it.isFilled }) {
            drawingCompleteListener?.onDrawingComplete()

        }
    }

    fun setFillColor(color: Int) {
        fillPaint.color = color
    }

    fun undo(): Boolean {
        val last = regions.lastOrNull { it.isFilled } ?: return false
        last.isFilled = false
        last.fillColor = Color.WHITE
        invalidate()
        return true
    }
}
