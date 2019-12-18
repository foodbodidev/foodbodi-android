package com.foodbodi.controller

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Adapter
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.foodbodi.Base.BaseFragment
import com.foodbodi.R
import com.foodbodi.apis.FoodBodiResponse
import com.foodbodi.apis.FoodbodiRetrofitHolder
import com.foodbodi.apis.ReservationResponse
import com.foodbodi.apis.RestaurantResponse
import com.foodbodi.model.Reservation
import com.foodbodi.utils.Utils
import kotlinx.android.synthetic.main.reservation_fragment.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response



class ReservationFragment: BaseFragment() {

    private  var listReservation: ArrayList<Reservation> = ArrayList()
    var adapter: CaloriesIntakeAdapter = CaloriesIntakeAdapter(listReservation)

    var cursor: String = ""
    var isLoadingNextPage = false

    var isLoad: Boolean = false


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.reservation_fragment, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // RecyclerView node initialized here
        list_recycler_view.apply {
            layoutManager = LinearLayoutManager(activity)

            adapter = CaloriesIntakeAdapter(listReservation)
        }

        setUpScrollView()
        if (!isLoad) {
            this.getReservation()
            isLoad = true
        } else {
            progressBar.visibility = View.GONE
        }

    }


    private fun setUpScrollView() {
        list_recycler_view.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {

                val linearLayoutManager = recyclerView.layoutManager as LinearLayoutManager
                if (isLoadingNextPage) {
                    var lastItem = linearLayoutManager.findLastCompletelyVisibleItemPosition()
                    if (lastItem == listReservation.size - 1) {
                        //bottom of list!
                        loadMore()
                    }
                }
            }
        })
    }

    fun loadMore() {
        progressBar.visibility = View.VISIBLE
        getReservation()
    }

    private fun getReservation() {
        FoodbodiRetrofitHolder.getService().getReservation(FoodbodiRetrofitHolder.getHeaders(this.requireContext()), cursor)
            .enqueue(object : Callback<FoodBodiResponse<ReservationResponse>> {
                override fun onFailure(call: Call<FoodBodiResponse<ReservationResponse>>, t: Throwable) {
                    Utils.showAlert(t.message!!, requireActivity())
                    progressBar.visibility = View.GONE
                }

                override fun onResponse(
                    call: Call<FoodBodiResponse<ReservationResponse>>,
                    response: Response<FoodBodiResponse<ReservationResponse>>
                ) {
                    val progressBar = progressBar ?: return
                    progressBar.visibility = View.GONE
                    if (FoodBodiResponse.SUCCESS_CODE == response.body()?.statusCode()) {
                        val data = response.body()?.data()?.reservation ?: return
                        val cursorResponse = response.body()?.data()?.cursor ?: ""

                        if (cursorResponse == cursor){
                            isLoadingNextPage = false

                        } else {
                            isLoadingNextPage = (data.size != 0)
                            cursor = cursorResponse
                        }

                        listReservation.addAll(data)


                        if (list_recycler_view != null) {
                            (list_recycler_view.adapter as CaloriesIntakeAdapter).reloadData(listReservation)
                        }

                    }
                }

            })

    }

}