package com.example.myapplication.CustomView.Tracing
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import androidx.annotation.DrawableRes
import kotlin.math.atan2
class PenNib(context: Context, @DrawableRes resId: Int, val shouldRotate: Boolean) {
    private val bitmap: Bitmap
    private var x: Float = 100f
    private var y: Float = 100f
    private var rotation: Float = 0f // Góc xoay tính bằng độ

    init {
        val original = BitmapFactory.decodeResource(context.resources, resId)
        // Scale down the bitmap to appropriate size (adjust size as needed)
        val scale = 2.0f
        bitmap = Bitmap.createScaledBitmap(
            original,
            (original.width * scale).toInt(),
            (original.height * scale).toInt(),
            true
        )
        original.recycle()
    }

    fun updatePosition(x: Float, y: Float, tanVector: FloatArray?) {
        this.x = x
        this.y = y
        tanVector?.let {
            rotation = Math.toDegrees(atan2(it[1].toDouble(), it[0].toDouble())).toFloat()
        }
    }

    fun draw(canvas: Canvas) {
        canvas.save()
        canvas.translate(x, y)

        if (shouldRotate) {
            canvas.rotate(rotation)
        }

        // Draw centered at the tip (adjust offset as needed)
        canvas.drawBitmap(bitmap, 0f, 0f, null)
        canvas.restore()
    }
}