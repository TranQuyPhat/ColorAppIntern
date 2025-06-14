package com.example.myapplication.model

import android.graphics.*

data class TracingRegion(
    val id: Int,
    val path: Path,
    val bounds: RectF,
    var isFilled: Boolean = false,
    var fillColor: Int = Color.TRANSPARENT,
    val label: String? = null,
    val expectedColor: Int? = null
)
