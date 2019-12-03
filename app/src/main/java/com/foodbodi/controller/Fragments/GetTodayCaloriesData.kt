package com.foodbodi.controller.Fragments

import android.app.Activity
import android.content.Context
import android.util.Log
import com.foodbodi.apis.FoodBodiResponse
import com.foodbodi.apis.FoodbodiRetrofitHolder
import com.foodbodi.apis.ReservationResponse
import com.foodbodi.model.DailyLog
import com.foodbodi.model.LocalDailyLogDbManager
import com.foodbodi.model.Reservation
import com.foodbodi.utils.Action
import com.foodbodi.utils.DateString
import com.foodbodi.utils.fitnessAPI.FitnessAPI
import com.foodbodi.utils.fitnessAPI.FitnessAPIFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import java.util.concurrent.*
import kotlin.collections.ArrayList

class GetTodayCaloriesData(val username:String, val activity: Activity) {
    val TAG:String = GetTodayCaloriesData::class.java.simpleName
    fun getRemainCalories(cb:Action<Int>) {

        val todayLog:DailyLog = LocalDailyLogDbManager.ensureLocalDailyLogRecord(DateString.fromCalendar(Calendar.getInstance()), this.username)
        var threshold = todayLog.calo_threshold
        if (threshold == null) threshold = 3000

        getReservations(object : Action<ArrayList<Reservation>> {
            override fun accept(reservations: ArrayList<Reservation>?) {
                var total:Int? = reservations?.map { item -> item.total }?.sumBy { value -> if( value != null) value else 0 }
                if (total == null) total = 0;
                val fitnessAPI = FitnessAPIFactory.getByProvider()
                fitnessAPI.setActivity(activity)
                Log.i(TAG, "Loading today step count")
                fitnessAPI.getTodayStepCount(object : Action<Int> {
                    override fun accept(data: Int?) {
                        if (data != null) {
                            cb.accept(threshold - total + data * 25 / 1000)
                        } else {
                            cb.accept(threshold - total)
                        }
                    }

                    override fun deny(data: Int?, reason: String) {
                        Log.i(TAG, reason)
                        cb.accept(0)
                    }

                })
            }

            override fun deny(data: ArrayList<Reservation>?, reason: String) {
                cb.deny(null, reason)
            }

        })

    }

    fun getReservations(cb:Action<ArrayList<Reservation>>) {
        Log.i(TAG, "Loading calory intakes")
        FoodbodiRetrofitHolder.getService()
            .getReservation(FoodbodiRetrofitHolder.getHeaders(activity))
            .enqueue(object : Callback<FoodBodiResponse<ReservationResponse>> {
                override fun onFailure(call: Call<FoodBodiResponse<ReservationResponse>>, t: Throwable) {
                    cb.deny(null, "Can not get calories intakes list")
                }

                override fun onResponse(
                    call: Call<FoodBodiResponse<ReservationResponse>>,
                    response: Response<FoodBodiResponse<ReservationResponse>>
                ) {
                    if (FoodBodiResponse.SUCCESS_CODE == response.body()?.statusCode()) {
                        val resercationRes: ReservationResponse? = response.body()?.data()
                        cb.accept(resercationRes?.reservation)
                    } else {
                        cb.deny(null, response.body()?.errorMessage!!)
                    }
                }

            })
    }
}