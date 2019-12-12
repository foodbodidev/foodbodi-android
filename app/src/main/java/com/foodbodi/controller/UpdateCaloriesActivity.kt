package com.foodbodi.controller

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.foodbodi.Adapters.CaloriesCardAdapter
import com.foodbodi.Adapters.CaloriesCartDelegate
import com.foodbodi.Base.BaseActivity
import com.foodbodi.R
import com.foodbodi.apis.FoodBodiResponse
import com.foodbodi.apis.FoodCardResonse
import com.foodbodi.apis.FoodbodiRetrofitHolder
import com.foodbodi.apis.UpdateCaloriesResponse
import com.foodbodi.apis.requests.ReservationRequest
import com.foodbodi.model.CurrentUserProvider
import com.foodbodi.model.Food
import com.foodbodi.model.FoodCartModel
import com.foodbodi.utils.DateString
import com.foodbodi.utils.Utils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.ArrayList


class UpdateCaloriesActivity : BaseActivity(), CaloriesCartDelegate  {

    private  var myDataset: ArrayList<Food> = ArrayList()

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager

    var reservationButton: Button? = null
    var backButton: Button? = null
    var totalTextView: TextView? = null
    var reservationId: String = ""
    var restaurantId: String = ""
    var isUpdateCalories: Boolean = true


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_calories)

        reservationId = intent.getStringExtra("reservation_id")?: ""
        restaurantId = intent.getStringExtra("restaurant_id")?: ""
        isUpdateCalories = intent.getBooleanExtra("isUpdateCalories", true)


        if (isUpdateCalories) {
            getReservationById()
        } else {
            myDataset = intent.getSerializableExtra("foodDisplay") as ArrayList<Food>
        }

        viewManager = LinearLayoutManager(this)
        viewAdapter = CaloriesCardAdapter(myDataset, this)

        recyclerView = findViewById<RecyclerView>(R.id.cart_recycler_view).apply {
            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter

        }
        configureLayout()
        setupActionUpdateCart()
        setupActionBack()

    }




    private fun configureLayout() {
        reservationButton = findViewById(R.id.button_reservation)
        totalTextView = findViewById(R.id.total_calories)
        backButton = findViewById(R.id.button_back)

        var reservationButtonUw = reservationButton ?: return

        if (isUpdateCalories) {
            reservationButtonUw.text = "UPDATE"
            reservationButtonUw.setBackgroundColor(ContextCompat.getColor(this, R.color.high_calo))

        } else {
            reservationButtonUw.text = "RESERVATION"
            reservationButtonUw.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary))
        }
    }


    private fun setupActionBack() {
        backButton?.setOnClickListener(object: View.OnClickListener {
            override fun onClick(v: View?) {
                onBackPressed()
            }

        })
    }

    fun setupActionUpdateCart() {
        reservationButton?.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                val dataList = (viewAdapter as CaloriesCardAdapter).myDataset


                val request = ReservationRequest()

                request.date_string = DateString.fromCalendar(Calendar.getInstance()).getString()
                request.restaurantId = restaurantId
                for (element in dataList) {
                    var foodCart = FoodCartModel()
                    foodCart.food_id = element.id
                    foodCart.amount = element.amount
                    request.foods.add(foodCart)
                }

                if (isUpdateCalories) {
                    updateReservationById(request)
                } else {
                    addReservation(request)
                }

            }

        })
    }

    fun updateReservationById(request: ReservationRequest) {
        showLoading(this)
        FoodbodiRetrofitHolder.getService().updateReservationById(FoodbodiRetrofitHolder.getHeaders(this@UpdateCaloriesActivity), request, reservationId)
            .enqueue(object : Callback<FoodBodiResponse<UpdateCaloriesResponse>> {

                override fun onFailure(call: Call<FoodBodiResponse<UpdateCaloriesResponse>>, t: Throwable) {
                    Utils.showAlert(t.message!!, this@UpdateCaloriesActivity)
                    hideLoading()
                    print(t.message)
                }

                override fun onResponse(
                    call: Call<FoodBodiResponse<UpdateCaloriesResponse>>,
                    response: Response<FoodBodiResponse<UpdateCaloriesResponse>>
                ) {
                    hideLoading()

                    val response = response.body() ?: return

                    if (FoodBodiResponse.SUCCESS_CODE == response.statusCode()) {
                        CurrentUserProvider.get().updateRemainCaloToEat(this@UpdateCaloriesActivity)
                        onBackPressed()

                    } else {
                       // Utils.showAlert(response.errorMessage, this@UpdateCaloriesActivity)
                    }
                }
            })
    }

    private fun addReservation(request: ReservationRequest) {
        showLoading(this)
        FoodbodiRetrofitHolder.getService().addReservation(FoodbodiRetrofitHolder.getHeaders(this@UpdateCaloriesActivity), request)
            .enqueue(object : Callback<FoodBodiResponse<UpdateCaloriesResponse>> {

                override fun onFailure(call: Call<FoodBodiResponse<UpdateCaloriesResponse>>, t: Throwable) {
                    Utils.showAlert(t.message!!, this@UpdateCaloriesActivity)
                    hideLoading()
                    print(t.message)
                }

                override fun onResponse(
                    call: Call<FoodBodiResponse<UpdateCaloriesResponse>>,
                    response: Response<FoodBodiResponse<UpdateCaloriesResponse>>
                ) {
                    hideLoading()

                    val response = response.body() ?: return

                    if (FoodBodiResponse.SUCCESS_CODE == response.statusCode()) {
                        CurrentUserProvider.get().updateRemainCaloToEat(this@UpdateCaloriesActivity)
                        onBackPressed()

                    } else {
                        //Utils.showAlert(response.errorMessage, this@UpdateCaloriesActivity)
                    }
                }
            })
    }

    private  fun getReservationById() {

        showLoading(this)

        FoodbodiRetrofitHolder.getService().getReservationById(FoodbodiRetrofitHolder.getHeaders(this@UpdateCaloriesActivity), reservationId)
            .enqueue(object : Callback<FoodBodiResponse<FoodCardResonse>> {

                override fun onFailure(call: Call<FoodBodiResponse<FoodCardResonse>>, t: Throwable) {
                    // Toast.makeText(this.require`, t.message, Toast.LENGTH_LONG).show()
                    hideLoading()
                    Utils.showAlert(t.message!!, this@UpdateCaloriesActivity)
                }

                override fun onResponse(
                    call: Call<FoodBodiResponse<FoodCardResonse>>,
                    response: Response<FoodBodiResponse<FoodCardResonse>>
                ) {
                    hideLoading()
                    if (FoodBodiResponse.SUCCESS_CODE == response.body()?.statusCode()) {

                        val data = response.body()?.data

                        val listFood: ArrayList<Food> = ArrayList()

                        val listFoodHasMap = data?.foods?.values

                        val amountFoodList = data?.reservation?.foods

                        listFoodHasMap?.forEach {
                            listFood.add(it)
                        }

                        for (i in 0..listFood.size - 1) {

                            for (j in 0..amountFoodList!!.size - 1) {

                                if (listFood[i].id == amountFoodList[j].food_id) {
                                    listFood[i].amount = amountFoodList[j].amount
                                }
                            }
                        }

                        // bind data
                        val adapter = recyclerView.adapter as CaloriesCardAdapter
                        adapter.reloadData(listFood)

                        totalTextView?.text = data?.reservation?.total.toString() + " Kcal"

                    }
                }

            })

    }

    // confront interface to listen action calculate calories

    override fun didCaculateTotalCalories(totalCalories: Int) {
        totalTextView?.text = totalCalories.toString() + " Kcal"
    }

}


