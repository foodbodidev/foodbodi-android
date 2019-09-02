package com.foodbodi.utils

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.Toast
import com.foodbodi.R
import kotlinx.android.synthetic.main.list_food_item.view.*

abstract class DynamicLinearLayoutController(val root:LinearLayout, var itemContainerId:Int, var itemContentId:Int) {
    val itemRenderer:HashMap<String, Renderer<Any>> = HashMap<String, Renderer<Any>>()
    val data:ArrayList<Any> = ArrayList()

    fun setRenderer(type:String, renderer:Renderer<Any>) : DynamicLinearLayoutController {
        itemRenderer.put(type, renderer)
        return this
    }

    fun addItem(type: String, data:Any) {
        var renderer = itemRenderer.get(type)
        val view:View? = renderer?.getView(data)

        if (view != null) {
            root.addView(view)
            val pos = this.data.size
            var child:LinearLayout = view.findViewById<LinearLayout>(this.itemContentId)
            child.setOnTouchListener(object : SwipeEvent() {
                override fun onClick(view: View, event: MotionEvent) {
                    swipeCancel(view, event)
                }

                override fun onLongClick(view: View, event: MotionEvent) {
                    swipeCancel(view, event)
                }

                override fun onLeftSwipe(view: View) {
                    onItemLeftSwipe(pos, view)
                }

                override fun onRightSwipe(view: View) {
                    onItemRightSwipe(pos, view)
                }

            })

            this.data.add(data)
        }
    }

    fun removeItem(pos:Int) {
        root.removeViewAt(pos)
        this.data.removeAt(pos)
    }

    abstract fun onItemLeftSwipe(pos:Int, view: View)
    abstract fun onItemRightSwipe(pos: Int, view: View)

}

interface Renderer<T:Any> {
    fun getView(data:T) : View?
}
