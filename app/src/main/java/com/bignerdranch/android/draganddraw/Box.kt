package com.bignerdranch.android.draganddraw

import android.graphics.PointF
import android.graphics.Rect
import android.graphics.RectF
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Box(val start: PointF, var rotation: Float = 0F) : Parcelable {

    var end: PointF = start

    val left: Float
        get() = Math.min(start.x, end.x)

    val right: Float
        get() = Math.max(start.x, end.x)

    val top: Float
        get() = Math.min(start.y, end.y)

    val bottom: Float
        get() = Math.max(start.y, end.y)

    val center: PointF
        get() {
            val rect = RectF(left, top, right, bottom)
            return PointF(rect.centerX(), rect.centerY())
        }
}

//canvas.drawRect(
//box.left,
//box.top,
//box.right,
//box.bottom,
//boxPaint
//)


// --------
// |      |
// |      |
// --------