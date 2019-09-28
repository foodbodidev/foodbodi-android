package com.foodbodi.controller

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Adapter
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.foodbodi.R
import com.foodbodi.apis.FoodBodiResponse
import com.foodbodi.apis.FoodbodiRetrofitHolder
import com.foodbodi.apis.ReservationResponse
import com.foodbodi.apis.RestaurantResponse
import com.foodbodi.model.Reservation
import kotlinx.android.synthetic.main.reservation_fragment.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response



class ReservationFragment:Fragment() {



    private  var myDataset: ArrayList<Reservation> = ArrayList()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.reservation_fragment, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // RecyclerView node initialized here
        list_recycler_view.apply {
            layoutManager = LinearLayoutManager(activity)

            adapter = CaloriesIntakeAdapter(myDataset)
        }


        this.getReservation()
    }

    fun getReservation() {
        FoodbodiRetrofitHolder.getService().getReservation(FoodbodiRetrofitHolder.getHeaders(this.requireContext()))
            .enqueue(object : Callback<FoodBodiResponse<ReservationResponse>> {
                override fun onFailure(call: Call<FoodBodiResponse<ReservationResponse>>, t: Throwable) {
                   // Toast.makeText(this.require`, t.message, Toast.LENGTH_LONG).show()
                }

                override fun onResponse(
                    call: Call<FoodBodiResponse<ReservationResponse>>,
                    response: Response<FoodBodiResponse<ReservationResponse>>
                ) {
                    if (FoodBodiResponse.SUCCESS_CODE == response.body()?.statusCode()) {
                        val data = response.body()?.data()?.reservation
                        if (data != null && list_recycler_view != null) {
                            (list_recycler_view.adapter as CaloriesIntakeAdapter).reloadData(data);
                        }

                    }
                }

            })
    }

}