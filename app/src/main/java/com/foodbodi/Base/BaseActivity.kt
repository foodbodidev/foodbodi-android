package com.foodbodi.Base

import android.app.Activity
import android.app.ProgressDialog
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.foodbodi.Adapters.CaloriesCardAdapter
import com.foodbodi.R
import com.foodbodi.apis.FoodBodiResponse
import com.foodbodi.apis.FoodCardResonse
import com.foodbodi.apis.FoodbodiRetrofitHolder
import com.foodbodi.apis.UpdateCaloriesResponse
import com.foodbodi.apis.requests.ReservationRequest
import com.foodbodi.model.Food
import com.foodbodi.model.FoodCartModel
import com.foodbodi.utils.Utils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.ArrayList



open class BaseActivity : AppCompatActivity() {


    //var progressBar: ProgressBar? = null
    var progressDialog: ProgressDialog? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val view = window.decorView.rootView
        if ( view != null && this != null) {
            setupLayout(view!!)
        }

    }

    //dismiss keyboard when clicking outside
    private fun setupLayout(view: View) {
        // Set up touch listener for non-text box views to hide keyboard.
        if (!(view is EditText)) {
            view.setOnTouchListener(object : View.OnTouchListener {
                override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                    if (this != null ) {
                        Utils.hideSoftKeyboard(this@BaseActivity)
                    }
                    return false
                }
            })
        }

        //If a layout container, iterate over children and seed recursion.
        if (view is ViewGroup) {

            for(viewElement in view.children) {
                setupLayout(viewElement)
            }
        }
    }


    fun showLoading(context: Activity) {
        progressDialog = ProgressDialog(context)
        progressDialog?.setMessage("Loading")
        progressDialog?.show()
    }

    fun hideLoading() {
        progressDialog?.hide()
    }




}
