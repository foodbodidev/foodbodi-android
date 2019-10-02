package com.foodbodi.controller

import android.os.Bundle
import android.util.Log
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager
import com.foodbodi.R
import com.foodbodi.model.Reservation
import com.foodbodi.Adapters.CaloriesCardAdapter


import kotlinx.android.synthetic.main.activity_update_calories.*
import kotlinx.android.synthetic.main.activity_update_calories.cart_recycler_view
import kotlinx.android.synthetic.main.reservation_fragment.*
import androidx.recyclerview.widget.RecyclerView
import com.foodbodi.apis.FoodBodiResponse
import com.foodbodi.apis.FoodCardResonse
import com.foodbodi.apis.FoodbodiRetrofitHolder
import com.foodbodi.model.Food
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.content.Intent
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.Button


class UpdateCaloriesActivity : AppCompatActivity() {

    private  var myDataset: ArrayList<Food> = ArrayList()

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager

    var reservationButton: Button? = null
    var reservationId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_calories)
        reservationButton = findViewById(R.id.button_reservation)
        reservationId = intent.getStringExtra("reservation_id")

        viewManager = LinearLayoutManager(this)
        viewAdapter = CaloriesCardAdapter(myDataset)

        recyclerView = findViewById<RecyclerView>(R.id.cart_recycler_view).apply {
            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter

        }


        setupActionUpdateCart()

        getReservationById()

    }

    fun setupActionUpdateCart() {
        reservationButton?.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                val dataList = (viewAdapter as CaloriesCardAdapter).myDataset

                print(dataList[0].amount)
            }

        })
    }

    fun getReservationById() {
        FoodbodiRetrofitHolder.getService().getReservationById(FoodbodiRetrofitHolder.getHeaders(this@UpdateCaloriesActivity), reservationId)
            .enqueue(object : Callback<FoodBodiResponse<FoodCardResonse>> {
                override fun onFailure(call: Call<FoodBodiResponse<FoodCardResonse>>, t: Throwable) {
                    // Toast.makeText(this.require`, t.message, Toast.LENGTH_LONG).show()
                }

                override fun onResponse(
                    call: Call<FoodBodiResponse<FoodCardResonse>>,
                    response: Response<FoodBodiResponse<FoodCardResonse>>
                ) {
                    if (FoodBodiResponse.SUCCESS_CODE == response.body()?.statusCode()) {

                        val data = response.body()?.data

                        val listFood: ArrayList<Food> = ArrayList()

                        val listFoodHasMap = data?.foods?.values

                        listFoodHasMap?.forEach {
                            listFood.add(it)
                        }

                        val adapter = recyclerView.adapter as CaloriesCardAdapter
                        adapter.reloadData(listFood)


                    }
                }

            })
    }

}
