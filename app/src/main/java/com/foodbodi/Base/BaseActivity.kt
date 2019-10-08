package com.foodbodi.Base

import android.app.Activity
import android.app.ProgressDialog
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
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
import com.foodbodi.utils.DateUtils
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
        //setContentView(R.layout.layout_loading_dialog)



    }

//    fun setupProgressBar() {
//        progressBar = findViewById<ProgressBar>(R.id.progressBarBase)
//        progressBar?.visibility = View.VISIBLE
//    }

    fun showLoading(context: Activity) {
        progressDialog = ProgressDialog(context)
        progressDialog?.setMessage("Loading")
        progressDialog?.show()
    }

    fun hideLoading() {
        progressDialog?.hide()
    }




}
