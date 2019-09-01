package com.foodbodi.utils

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.Toast

abstract class DynamicLinearLayoutController(val root:LinearLayout) {
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
            val container = createItemContainer()
            container.addView(view)
            root.addView(container)
            val pos = this.data.size
            view.setOnTouchListener(object : SwipeEvent() {
                override fun onClick(view: View) {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

                override fun onLongClick(view: View) {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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

    private fun createItemContainer() : ViewGroup {
        val itemContainer = LinearLayout(root.context)
        itemContainer.setLayoutParams(LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))
        itemContainer.background = ColorDrawable(Color.GRAY)
        return itemContainer

    }

    abstract fun onItemLeftSwipe(pos:Int, view: View)
    abstract fun onItemRightSwipe(pos: Int, view: View)

}

interface Renderer<T:Any> {
    fun getView(data:T) : View?
}
