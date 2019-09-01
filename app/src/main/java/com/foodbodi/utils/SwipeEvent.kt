package com.foodbodi.utils

import android.view.MotionEvent
import android.view.View
import android.widget.Toast



abstract class SwipeEvent() : View.OnTouchListener {
    val CLICK_DURATION:Long = 3000
    val SWIPE_DISTANCE = 150;
    abstract fun onClick(view: View)
    abstract fun onLongClick(view: View)
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
                x1 = event.getX()
                y1 = event.getY()
                t1 = System.currentTimeMillis()
                moving = true
                return true
            }
            MotionEvent.ACTION_UP -> {
                x2 = event.getX()
                y2 = event.getY()
                t2 = System.currentTimeMillis()
                moving = false

                if (x1 == x2 && y1 == y2 ) {
                    if (t2 - t1 < CLICK_DURATION) {
                        onClick(view)
                        //Click
                    } else {
                        onLongClick(view)
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
        val offset = event.getX() - x1
        view.setTranslationX(offset)

    }

    fun swipeCancel(view:View, event: MotionEvent) {
        view.setTranslationX(0f)
    }

}