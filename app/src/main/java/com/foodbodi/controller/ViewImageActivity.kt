package com.foodbodi.controller

import android.media.Image
import android.os.Bundle
import android.view.ViewTreeObserver
import android.widget.ImageView
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import com.foodbodi.R
import com.foodbodi.utils.PhotoGetter
import com.foodbodi.utils.PhotoGetter.Companion.loadImageFromURL
import com.google.android.material.tabs.TabLayout
import com.squareup.picasso.Picasso

import kotlinx.android.synthetic.main.activity_view_image.*

class ViewImageActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_image)

        // Get the Intent that started this activity and extract the string
        val mUrl = intent.getStringExtra("key_url_image")
        val imageView = findViewById<ImageView>(R.id.imgDisplay)
        if (mUrl != null) {
            Picasso.get().load(mUrl).fit().centerCrop().into(imageView)
        }
    }

}
