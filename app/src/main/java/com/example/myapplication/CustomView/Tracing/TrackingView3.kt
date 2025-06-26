package com.example.myapplication.CustomView.Tracing

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.annotation.DrawableRes
import com.example.myapplication.Animation.TracingAnimator
import com.example.myapplication.R
import com.example.myapplication.model.TracingRegion

class TracingView3(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {
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

    private lateinit var penNib: PenNib
    private val pathManager = PathManager()
    private lateinit var animatorHelper: TracingAnimator

    private var viewWidth = 0
    private var viewHeight = 0
    private var transformationMatrix = Matrix()

    init {
        setLayerType(LAYER_TYPE_HARDWARE, null)
        penNib = PenNib(context, R.drawable.ic_pen1, shouldRotate = false)
    }

    fun setPenNibResource(@DrawableRes resId: Int) {
        penNib = PenNib(context, resId, shouldRotate = false)
        invalidate()
    }

    fun setFillColor(color: Int) {
        fillPaint.color = color
    }

    fun loadSVGFromAsset(inputStream: java.io.InputStream) {
        pathManager.loadSVG(inputStream)
        originalPathsToRegions(pathManager.originalPaths)
        if (viewWidth > 0 && viewHeight > 0) {
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
        animatorHelper = TracingAnimator(
            this, regions, fillPaint, penNib,
            onFillComplete = { checkDrawingComplete() },
            onDrawingAnimationComplete = {}
        )
        animatorHelper.prepareAnimation()
    }

    private val nextUnfilledRegion: TracingRegion?
        get() = regions.firstOrNull { !it.isFilled }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        regions.forEach { region ->
            if (region == nextUnfilledRegion && animatorHelper.isDrawingAnimationComplete) {
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
            } else if (region == animatorHelper.animatingRegion && animatorHelper.fillCenter != null) {
                val saveCount = canvas.save()
                val clipPath = Path().apply {
                    addCircle(animatorHelper.fillCenter!!.x, animatorHelper.fillCenter!!.y, animatorHelper.fillRadius, Path.Direction.CW)
                }
                canvas.clipPath(clipPath)
                canvas.drawPath(region.path, Paint(fillPaint).apply { color = fillPaint.color })
                canvas.restoreToCount(saveCount)
            }
        }

        if (!animatorHelper.isDrawingAnimationComplete) {
            canvas.drawPath(animatorHelper.getAnimatedPath(), animationPaint)
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

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (!animatorHelper.isDrawingAnimationComplete) {
                    animatorHelper.startDrawingAnimation()
                    return true
                }
                val x = event.x.toInt()
                val y = event.y.toInt()
                val sortedRegions = regions.sortedByDescending { it.id }
                for (region in sortedRegions) {
                    val targetRegion = nextUnfilledRegion
                    if (targetRegion != null) {
                        val hitRegion = Region().apply {
                            setPath(targetRegion.path, Region(0, 0, width, height))
                        }
                        if (hitRegion.contains(x, y)) {
                            animatorHelper.startFillAnimation(x.toFloat(), y.toFloat(), targetRegion)
                            return true
                        }
                    }
                }
            }
            MotionEvent.ACTION_UP -> {
                if (animatorHelper.isAnimating) {
                    animatorHelper.stopDrawingAnimation()
                    return true
                }
            }
        }
        return super.onTouchEvent(event)
    }

    fun undo(): Boolean {
        val last = regions.lastOrNull { it.isFilled } ?: return false
        last.isFilled = false
        last.fillColor = Color.WHITE
        invalidate()
        return true
    }
    fun getDrawingCacheBitmap(): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        draw(canvas)
        return bitmap
    }
    private fun checkDrawingComplete() {
        if (regions.all { it.isFilled }) {
            drawingCompleteListener?.onDrawingComplete()
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        viewWidth = w
        viewHeight = h
        if (regions.isNotEmpty()) {
            transformRegions()
            prepareAnimation()
        }
    }
    fun exportState(): List<TracingRegion> {
        return regions.map { it.copy() }
    }

    fun importState(saved: List<TracingRegion>) {
        regions.clear()
        regions.addAll(saved.map { it.copy() })
        invalidate()
    }
}