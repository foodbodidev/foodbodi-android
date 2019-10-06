package com.foodbodi.utils

import android.view.MotionEvent
import android.view.View
import android.widget.Toast



abstract class SwipeEvent() : View.OnTouchListener {
    val CLICK_DURATION:Long = 3000
    val SWIPE_DISTANCE = 150;
    abstract fun onClick(view: View, event: MotionEvent)
    abstract fun onLongClick(view: View, event: MotionEvent)
    abstract fun onLeftSwipe(view: View)
    abstract fun onRightSwipe(view: View)
    var x1:Float = 0f
    var x2:Float = 0f
    var y1:Float = 0f
    var y2:Float = 0f
    var t1:Long = 0
    var t2:Long = 0
    var moving:Boolean = false

    override fun onTouch(view: View, event: MotionEvent): Boolean {
        if (moving) {
            onMoveView(view, event)
        } else {
            swipeCancel(view, event)
        }
        when (event.getAction()) {
            MotionEvent.ACTION_DOWN -> {
                x1 = event.getRawX()
                y1 = event.getRawY()
                t1 = System.currentTimeMillis()
                startMoving(view)
                return true
            }
            MotionEvent.ACTION_UP -> {
                x2 = event.getRawX()
                y2 = event.getRawY()
                t2 = System.currentTimeMillis()
                stopMoving(view)
                if (x1 == x2 && y1 == y2 ) {
                    if (t2 - t1 < CLICK_DURATION) {
                        onClick(view, event)
                        //Click
                    } else {
                        onLongClick(view, event)
                        //Long click
                    }
                } else if (x1 > x2 && (x1 - x2) > SWIPE_DISTANCE) {
                    onLeftSwipe(view)
                    //Left swipe
                } else if (x2 > x1 && (x2 - x1) > SWIPE_DISTANCE) {
                    onRightSwipe(view)
                    //Right swipe
                } else {
                    swipeCancel(view, event)
                }

                return true
            }
        }

        return false
    }

    private fun onMoveView(view: View, event: MotionEvent) {
        val distance = (event.getRawX() - x1)
        view.setTranslationX(distance)

    }

    fun swipeCancel(view:View, event: MotionEvent) {
        stopMoving(view)
        resetValues()
    }

    private fun resetValues() {
         x1 = 0f
         x2 = 0f
         y1 = 0f
         y2 = 0f
         t1 = 0
         t2 = 0
    }

    private fun stopMoving(view: View) {
        moving = false;
        view.setTranslationX(0f)
    }

    private fun startMoving(view: View) {
        moving = true;
    }
}