package com.foodbodi

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.ImageView
import android.widget.Toast
import com.foodbodi.controller.ViewImageActivity
import com.foodbodi.utils.Action
import com.foodbodi.utils.PhotoGetter
import java.util.*


class RestaurantPhotoItem(var url:String?) : Fragment() {
    private var listener: OnFragmentInteractionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view:View =  inflater.inflate(R.layout.fragment_restaurant_photo_item, container, false)
        val imageView = view.findViewById<ImageView>(R.id.img_restaurant_photo)
        if (url != null) {
            val viewTreeObserver: ViewTreeObserver = imageView.viewTreeObserver
            viewTreeObserver.addOnGlobalLayoutListener {
                PhotoGetter.loadImageFromURL(url!!, imageView)
            }
        }
        imageView.setOnClickListener(object:View.OnClickListener{
            override fun onClick(v: View?) {
                val url = url
                val intent = Intent(context, ViewImageActivity::class.java).apply {
                    putExtra("key_url_image", url)
                }
                startActivity(intent)
            }

        })

        return view
    }



    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
    }
}
