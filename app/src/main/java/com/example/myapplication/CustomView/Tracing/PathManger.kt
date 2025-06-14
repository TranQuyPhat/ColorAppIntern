package com.example.myapplication.CustomView.Tracing


import android.graphics.*
import android.util.Xml
import androidx.core.graphics.PathParser
import org.xmlpull.v1.XmlPullParser
import java.io.InputStream

class PathManager {

    val originalPaths = mutableListOf<Path>()
    val transformedPaths = mutableListOf<Path>()
    val filledPaths = mutableSetOf<Path>()
    private var transformationMatrix = Matrix()

    var viewWidth = 0
    var viewHeight = 0

    fun loadSVG(inputStream: InputStream) {
        originalPaths.clear()
        val parser = Xml.newPullParser()
        parser.setInput(inputStream, null)

        var event = parser.eventType
        while (event != XmlPullParser.END_DOCUMENT) {
            if (event == XmlPullParser.START_TAG && parser.name == "path") {
                val d = parser.getAttributeValue(null, "d")
                try {
                    originalPaths.add(PathParser.createPathFromPathData(d))
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            event = parser.next()
        }

        if (viewWidth > 0 && viewHeight > 0) {
            transformPaths()
        }
    }

    fun transformPaths() {
        if (originalPaths.isEmpty()) return

        val totalBounds = RectF()
        val tempBounds = RectF()
        var first = true

        for (path in originalPaths) {
            path.computeBounds(tempBounds, true)
            if (first) {
                totalBounds.set(tempBounds)
                first = false
            } else {
                totalBounds.union(tempBounds)
            }
        }

        val scaleX = viewWidth / totalBounds.width()
        val scaleY = viewHeight / totalBounds.height()
        val scale = minOf(scaleX, scaleY) * 0.9f
        val tx = (viewWidth - totalBounds.width() * scale) / 2 - totalBounds.left * scale
        val ty = (viewHeight - totalBounds.height() * scale) / 2 - totalBounds.top * scale

        transformationMatrix.reset()
        transformationMatrix.postScale(scale, scale)
        transformationMatrix.postTranslate(tx, ty)

        transformedPaths.clear()
        for (path in originalPaths) {
            val transformed = Path()
            path.transform(transformationMatrix, transformed)
            transformedPaths.add(transformed)
        }
    }


}
