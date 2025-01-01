package com.bignerdranch.android.draganddraw

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.RectF
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.core.graphics.contains
import androidx.core.graphics.withRotation
import kotlin.math.atan2

private const val TAG = "BoxDrawingView"
const val CURRENT_SHAPES = "CURRENT_SHAPES"
const val CURRENT_STATE = "CURRENT_STATE"

enum class UserTouchType {
    NONE,
    TRANSLATE,
    SCALE
}

class BoxDrawingView(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private var currentUserTouchType = UserTouchType.NONE
    private val userLastTouchPoint = PointF()

    private var lastRotation = 0f
    private var isRotationStarted = false

    private var isRotationMode: Boolean = false
    var nonPrimaryCurrent: PointF? = null
    var startCurrent: PointF? = null

    private var currentBox: Box? = null
    private var boxes = mutableListOf<Box>()
    private val boxPaint = Paint().apply {
        color = 0x22ff0000
    }
    private val backgroundPaint = Paint().apply {
        color = 0xfff8efe0.toInt()
    }

    override fun onDraw(canvas: Canvas) {
        // Fill the background
        canvas.drawPaint(backgroundPaint)

        boxes.forEach { box ->
            canvas.withRotation(box.rotation, box.center.x, box.center.y) {
                canvas.drawRect(
                    box.left,
                    box.top,
                    box.right,
                    box.bottom,
                    boxPaint
                )
            }
        }
    }

    private fun log(event: MotionEvent) {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                Log.d(
                    TAG, "A_DOWN index: ${event.actionIndex}, " +
                            "ID: ${event.getPointerId(event.actionIndex)}"
                )
            }

            MotionEvent.ACTION_UP -> {
                Log.d(
                    TAG, "A_UP index: ${event.actionIndex}, " +
                            "ID: ${event.getPointerId(event.actionIndex)}"
                )
            }

            MotionEvent.ACTION_POINTER_DOWN -> {
                Log.d(
                    TAG, "A_POINTER_DOWN index: ${event.actionIndex}, " +
                            "ID: ${event.getPointerId(event.actionIndex)}"
                )
            }

            MotionEvent.ACTION_POINTER_UP -> {
                Log.d(
                    TAG, "A_POINTER_UP index: ${event.actionIndex}, " +
                            "ID: ${event.getPointerId(event.actionIndex)}"
                )
            }
        }
    }


    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event ?: return false
        val current = PointF(event.x, event.y)

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                log(event)
                currentBox = Box(current).also { box ->
                    boxes.add(box)
                }
                Log.d(TAG, boxes.toString())
            }

            MotionEvent.ACTION_UP -> {
                log(event)
                if (currentUserTouchType == UserTouchType.NONE) {
                    updateCurrentBox(current)
                    currentBox = null
                }
                currentBox = null
                currentUserTouchType = UserTouchType.NONE

                contentDescription = "Number of boxes on screen: ${boxes.size}"
            }

            MotionEvent.ACTION_POINTER_DOWN -> {
                log(event)
                boxes.removeLast()
                invalidate()
                isRotationStarted = true
                lastRotation = getRotation(event)
                currentUserTouchType = UserTouchType.SCALE
            }

            MotionEvent.ACTION_POINTER_UP -> {
                log(event)
                isRotationStarted = false
            }

            MotionEvent.ACTION_MOVE -> {
                if (currentUserTouchType != UserTouchType.SCALE) {
                    updateCurrentBox(current)
                }
                processUserMoveTouchOperation(event)
            }

            MotionEvent.ACTION_CANCEL -> {
                currentBox = null
            }
        }
        return true
    }

    private fun updateCurrentBox(current: PointF, rotation: Float = 0F) {
        currentBox?.let {
            it.end = current
            it.rotation = rotation
            invalidate()
        }
    }

    private fun processUserMoveTouchOperation(event: MotionEvent) {
        when (currentUserTouchType) {
            UserTouchType.TRANSLATE -> {
                translateImageWithMotion(event)
            }

            UserTouchType.SCALE -> {
                scaleImageWithMotion(event)
            }

            UserTouchType.NONE -> {

            }
        }
    }


    private fun getRotation(event: MotionEvent): Float {
        val firstPointer = PointF(event.getX(0), event.getY(0))
        val secondPointer = PointF(event.getX(1), event.getY(1))
        val deltaX = firstPointer.x - secondPointer.x
        val deltaY = firstPointer.y - secondPointer.y
        val degreeInRadians = atan2(deltaY, deltaX).toDouble()
        return Math.toDegrees(degreeInRadians).toFloat()
    }


    private fun translateImageWithMotion(event: MotionEvent) {
        // Calculate distance
    }

    private fun scaleImageWithMotion(event: MotionEvent) {
        if (event.pointerCount == 2 && isRotationStarted) {
            val currentRotation = getRotation(event)
            val newRotation = currentRotation - lastRotation
            // Logic for detecting box
            boxes.forEach { box ->
                with(box) {
                    val rect = RectF(left, top, right, bottom)

                    val firstPointer = PointF(event.getX(0), event.getY(0))
                    val secondPointer = PointF(event.getX(1), event.getY(1))

                    if (rect.contains(firstPointer) && rect.contains(secondPointer)) {
                        box.rotation += newRotation
                        invalidate()
                    }
                }
            }
            lastRotation = currentRotation
        }
    }

    override fun onSaveInstanceState(): Parcelable? {
        val bundle = Bundle()
        bundle.putParcelable(CURRENT_STATE, super.onSaveInstanceState())
        bundle.putParcelable(CURRENT_SHAPES, Shapes(boxes))
        return bundle
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        var updatedState: Parcelable? = state
        if (state is Bundle) {
            val shapes = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                state.getParcelable(CURRENT_SHAPES, Shapes::class.java)?.boxes?.toMutableList()
            } else {
                (state.getParcelable(CURRENT_SHAPES) as? Shapes)?.boxes?.toMutableList()
            }
            boxes = shapes ?: mutableListOf()
            updatedState = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                state.getParcelable(CURRENT_STATE, Parcelable::class.java)
            } else {
                state.getParcelable(CURRENT_STATE)
            }
        }

        super.onRestoreInstanceState(updatedState)
    }
}